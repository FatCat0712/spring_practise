package vn.tayjava.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

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

    public void sendConfirmLink(String email, Long id, String secretCode) throws MessagingException, UnsupportedEncodingException {
        log.info("Sending email confirmation account");
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

        Context context = new Context();
        String confirmLink = String.format("http://localhost:8080/users/confirm/%s?secretCode=%s", id, secretCode);

        Map<String, Object> properties = new HashMap<>();
        properties.put("confirmLink", confirmLink);
        context.setVariables(properties);

        helper.setFrom(senderEmail, "Eddie");
        helper.setTo(email);
        helper.setText("Please confirm your account by clicking the following link: " + confirmLink, true);
        helper.setSubject("Confirm your account");

        String htmlContent = templateEngine.process("confirm-email.html", context);
        helper.setText(htmlContent, true);

        mailSender.send(message);

        log.info("Email sent to {}", email);

    }

    @KafkaListener(topics = "confirm-account-topic", groupId = "confirm-account-group")
    public void sendConfirmLinkByKafka(String kafkaMessage) throws MessagingException, UnsupportedEncodingException {
        log.info("Sending email confirmation account, kafkaMessage: {}", kafkaMessage);

        String[] parts = kafkaMessage.split(",");
        String email = parts[0].split("=")[1];
        Long id = Long.parseLong(parts[1].split("=")[1]);
        String secretCode = parts[2].split("=")[1];

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

        Context context = new Context();
        String confirmLink = String.format("http://localhost:8080/users/confirm/%s?secretCode=%s", id, secretCode);

        Map<String, Object> properties = new HashMap<>();
        properties.put("confirmLink", confirmLink);
        context.setVariables(properties);

        helper.setFrom(senderEmail, "Eddie");
        helper.setTo(email);
        helper.setText("Please confirm your account by clicking the following link: " + confirmLink, true);
        helper.setSubject("Confirm your account");

        String htmlContent = templateEngine.process("confirm-email.html", context);
        helper.setText(htmlContent, true);

        mailSender.send(message);

        log.info("Email sent to email {}, link {}", email, confirmLink);

    }
}
