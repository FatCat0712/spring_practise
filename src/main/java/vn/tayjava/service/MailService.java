package vn.tayjava.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String senderEmail;

    public String sendEmail(String to, String subject, String body, MultipartFile[] files) throws MessagingException, UnsupportedEncodingException {
        log.info("Sending email to: {}, subject: {}, body: {}, files: {}", to, subject, body, files);
        // Implement email sending logic here using mailSender
        // For example, create a SimpleMailMessage and send it
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(senderEmail, "Eddie");

        if(to.contains(",")) {
            helper.setTo(InternetAddress.parse(to));
        } else {
            helper.setTo(to);
        }

        if(files != null) {
            for(MultipartFile file : files) {
                helper.addAttachment(Objects.requireNonNull(file.getOriginalFilename()), file);
            }
        }

        helper.setSubject(subject);
        helper.setText(body, true);

        mailSender.send(message);
        log.info("Email sent successfully to {}", to);
        return "sent";
    }
}
