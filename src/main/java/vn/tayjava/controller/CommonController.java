package vn.tayjava.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.tayjava.configuration.Translator;
import vn.tayjava.dto.response.ResponseData;
import vn.tayjava.dto.response.ResponseError;
import vn.tayjava.service.MailService;

@Slf4j
@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
public class CommonController {
    private final MailService mailService;

    @PostMapping("/send-email")
    public ResponseData<String> sendEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body,
            @RequestBody MultipartFile[] files
            ) {
        try {
            String result = mailService.sendEmail(to, subject, body, files);
            return new ResponseData<>(HttpStatus.ACCEPTED  .value(), "Email sent", result);
        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage());
            return new ResponseError<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to send email");
        }
    }
}
