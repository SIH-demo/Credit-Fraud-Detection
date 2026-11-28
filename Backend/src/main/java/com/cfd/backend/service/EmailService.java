
package com.cfd.backend.service;

import com.cfd.backend.enums.OtpPurpose;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String recipient, String otp, OtpPurpose purpose) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(recipient);

        if (purpose == OtpPurpose.REGISTRATION) {
            message.setSubject("Verify your email - CreditGuard");
            message.setText(
                "Hello,\n\n" +
                "Your CreditGuard email verification code is: " + otp + "\n\n" +
                "This code expires in 5 minutes. " +
                "Do not share this code with anyone.\n\n" +
                "If you did not request this verification, please ignore this email.\n\n" +
                "Regards,\nCreditGuard Team"
            );

        } else if (purpose == OtpPurpose.PASSWORD_RESET) {
            message.setSubject("Password reset verification - CreditGuard");
            message.setText(
                "Hello,\n\n" +
                "Your CreditGuard password reset code is: " + otp + "\n\n" +
                "This code expires in 5 minutes. " +
                "Do not share this code with anyone.\n\n" +
                "If you did not request a password reset, please ignore this email.\n\n" +
                "Regards,\nCreditGuard Team"
            );
        }

        mailSender.send(message);
    }
}
