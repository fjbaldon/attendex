package com.github.fjbaldon.attendex.platform.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Year;

@RequiredArgsConstructor
@Slf4j
public class EmailService { // Made public

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendReportEmail(String to, String subject, String body, String attachmentFilename, byte[] attachmentData) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true = multipart (needed for attachments)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            // This attaches the byte array as a file to the email
            helper.addAttachment(attachmentFilename, new ByteArrayResource(attachmentData));

            mailSender.send(message);
            log.info("Sent report email to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send report email to {}", to, e);
        }
    }

    void sendVerificationEmail(String to, String organizationName, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            final String verificationUrl = frontendUrl + "/verify?token=" + token;
            // Use a CDN or public URL for images in emails
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

    void sendPasswordResetEmail(String to, String organizationName, String tempPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("organizationName", organizationName);
            context.setVariable("tempPassword", tempPassword);
            context.setVariable("loginUrl", frontendUrl + "/login");
            context.setVariable("currentYear", Year.now().getValue());

            String htmlContent = templateEngine.process("password-reset-email", context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Your Password Has Been Reset");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent password reset email to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}", to, e);
        }
    }
}
