package com.github.fjbaldon.attendex.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // Inject Thymeleaf's template engine

    // It's best practice to get this from your application.properties
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // The method now accepts organizationName for better personalization
    public void sendVerificationEmail(String to, String organizationName, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        final String verificationUrl = frontendUrl + "/verify?token=" + token;

        // 1. Create a Thymeleaf "Context" to hold your variables
        Context context = new Context();
        context.setVariable("verificationUrl", verificationUrl);
        context.setVariable("organizationName", organizationName);
        context.setVariable("currentYear", Year.now().getValue());

        // 2. Process the HTML template with the variables
        // "verification-email" is the name of your html file (verification-email.html)
        String htmlContent = templateEngine.process("verification-email", context);

        // 3. Set the email details and send
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Verify Your AttendEx Account");
        helper.setText(htmlContent, true); // `true` indicates the content is HTML

        mailSender.send(message);
    }
}
