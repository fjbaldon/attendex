package com.github.fjbaldon.attendex.platform.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Year;

@RequiredArgsConstructor
@Slf4j
class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    void sendVerificationEmail(String to, String organizationName, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            final String verificationUrl = frontendUrl + "/verify?token=" + token;

            final String logoUrl = "https://raw.githubusercontent.com/fjbaldon/attendex/main/web/public/logo.png";

            Context context = new Context();
            context.setVariable("verificationUrl", verificationUrl);
            context.setVariable("organizationName", organizationName);
            context.setVariable("currentYear", Year.now().getValue());
            context.setVariable("logoUrl", logoUrl);

            String htmlContent = templateEngine.process("verification-email", context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Verify Your AttendEx Account");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}", to, e);
        }
    }
}
