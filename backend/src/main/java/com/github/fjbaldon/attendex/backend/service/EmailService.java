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
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String verificationUrl = "http://localhost:3000/verify?token=" + token; // Frontend URL

        String htmlContent = "<!DOCTYPE html>"
                + "<html lang='en'>"
                + "<head><meta charset='UTF-8'><title>AttendEx Account Verification</title></head>"
                + "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;'>"
                + "  <div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 40px; border-radius: 8px; text-align: center;'>"
                + "    <h2 style='color: #333;'>Welcome to AttendEx!</h2>"
                + "    <p style='color: #555; font-size: 16px;'>Thank you for registering. Please click the button below to verify your email address and activate your account.</p>"
                + "    <a href='" + verificationUrl + "' style='background-color: #2563eb; color: #ffffff; padding: 15px 25px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; font-size: 16px; font-weight: bold;'>Verify My Email</a>"
                + "    <p style='color: #888; font-size: 12px;'>If the button above does not work, copy and paste this link into your browser:</p>"
                + "    <p style='color: #888; font-size: 12px; word-break: break-all;'>" + verificationUrl + "</p>"
                + "    <hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>"
                + "    <p style='color: #aaa; font-size: 12px;'>If you did not register for an account, please ignore this email.</p>"
                + "  </div>"
                + "</body>"
                + "</html>";

        helper.setFrom("fjbaldon@gmail.com");
        helper.setTo(to);
        helper.setSubject("Verify Your AttendEx Account");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
