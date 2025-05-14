package vn.com.fpt.sep490_g28_summer2024_be.unittest.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;
import vn.com.fpt.sep490_g28_summer2024_be.service.email.EmailService;
import vn.com.fpt.sep490_g28_summer2024_be.utils.Email;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@MockBean(FirebaseServiceImpl.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmailServiceTest {
    @MockBean
    private JavaMailSender mailSender;

    @Autowired
    private EmailService emailService;

    @MockBean(name = "emailExecutor")
    private Executor emailExecutor;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        // Create a mock MimeMessage before each test
        mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        // Configure executor to run tasks immediately
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(emailExecutor).execute(any(Runnable.class));
    }

    @Test
    @DisplayName("ES_sendEmail_01")
    void sendEmail_shouldSendSuccessfully_whenValidEmailProvided() throws ExecutionException, InterruptedException {
        // Arrange
        Email testEmail = Email.builder()
                .email("test@example.com")
                .title("Test Subject")
                .body("Test Body")
                .build();

        // Configure mailSender to do nothing when send is called
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        CompletableFuture<Void> future = emailService.sendEmail(testEmail);
        
        // Wait for the async operation to complete
        future.get();

        // Assert
        // Verify that send was called exactly once
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        
        // Capture and verify the email contents
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        // No exception means success
        assertNull(future.get());
    }

    @Test
    @DisplayName("ES_sendEmail_02")
    void sendEmail_shouldThrowAppException_whenMessagingExceptionOccurs() {
        // Arrange
        Email testEmail = Email.builder()
                .email("test@example.com")
                .title("Test Subject")
                .body("Test Body")
                .build();

        // Configure mailSender to throw exception when send is called
        doThrow(new MessagingException("Failed to send email"))
            .when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        CompletableFuture<Void> future = emailService.sendEmail(testEmail);
        
        Exception exception = assertThrows(
            ExecutionException.class,
            () -> future.get(),
            "Expected sendEmail to throw ExecutionException"
        );
        
        // Verify the cause is AppException with correct error code
        assertTrue(exception.getCause() instanceof AppException);
        assertEquals(
            ErrorCode.HTTP_SEND_EMAIL_FAILED,
            ((AppException) exception.getCause()).getErrorCode(),
            "Should throw AppException with HTTP_SEND_EMAIL_FAILED error code"
        );
    }
}
