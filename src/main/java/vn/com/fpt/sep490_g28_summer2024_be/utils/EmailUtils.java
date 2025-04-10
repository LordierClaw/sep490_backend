package vn.com.fpt.sep490_g28_summer2024_be.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class EmailUtils {

    private final JavaMailSender javaMailSender;
    public void sendEmail(Email email) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setTo(email.getEmail());
        mimeMessageHelper.setSubject(email.getTitle());
        mimeMessageHelper.setText(email.getBody(), true);
        javaMailSender.send(mimeMessage);
    }
}
