package vn.com.fpt.sep490_g28_summer2024_be.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.utils.Email;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class DefaultEmailService implements EmailService{

    private final JavaMailSender javaMailSender;
    private final Executor emailExcutor;

    public DefaultEmailService(JavaMailSender javaMailSender,@Qualifier("emailExecutor") Executor emailExcutor) {
        this.javaMailSender = javaMailSender;
        this.emailExcutor = emailExcutor;
    }

    @Override
    @Async("emailExecutor")
    public CompletableFuture<Void> sendEmail(Email email){
        return CompletableFuture.runAsync(() -> {
            try {
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
                mimeMessageHelper.setTo(email.getEmail());
                mimeMessageHelper.setSubject(email.getTitle());
                mimeMessageHelper.setText(email.getBody(), true);
                javaMailSender.send(mimeMessage);
            }catch (MessagingException ms){
                throw new AppException(ErrorCode.HTTP_SEND_EMAIL_FAILED);
            }
        }, emailExcutor);
    }


}
