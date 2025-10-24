package com.github.fjbaldon.attendex.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String verificationUrl = "http://localhost:3000/verify?token=" + token; // Frontend URL

        String htmlContent = "<h3>Welcome to AttendEx!</h3>" +
                "<p>Thank you for registering. Please click the link below to verify your email address and activate your account:</p>" +
                "<a href=\"" + verificationUrl + "\">Verify My Email</a>" +
                "<p>If you did not register for an account, please ignore this email.</p>";

//        helper.setFrom("noreply@attendex.com"); // Can be any "from" address
        helper.setFrom("fjbaldon@gmail.com"); // Can be any "from" address
        helper.setTo(to);
        helper.setSubject("Verify Your AttendEx Account");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
