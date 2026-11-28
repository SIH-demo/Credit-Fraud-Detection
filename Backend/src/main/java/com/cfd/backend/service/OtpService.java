
package com.cfd.backend.service;

import com.cfd.backend.entity.AppUser;
import com.cfd.backend.entity.EmailOtp;
import com.cfd.backend.enums.OtpPurpose;
import com.cfd.backend.repository.EmailOtpRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private final EmailOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(
            EmailOtpRepository otpRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void sendOtp(String email, OtpPurpose purpose, AppUser user) {

        String normalizedEmail = email.trim().toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        var latestOtp = otpRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(
                        normalizedEmail, purpose);

        if (latestOtp.isPresent()) {
            LocalDateTime createdAt = latestOtp.get().getCreatedAt();

            if (createdAt != null &&
                    createdAt.plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(now)) {
                throw new IllegalStateException(
                        "Please wait before requesting another OTP.");
            }
        }

        // Invalidate all previous unused OTPs for this email and purpose.
        List<EmailOtp> previousOtps =
                otpRepository.findByEmailAndPurposeAndConsumedAtIsNull(
                        normalizedEmail, purpose);

        for (EmailOtp previous : previousOtps) {
            previous.setConsumedAt(now);
        }

        otpRepository.saveAll(previousOtps);

        // Generate a cryptographically secure six-digit OTP.
        String otp = String.format("%06d",
                secureRandom.nextInt(1_000_000));

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setUser(user);
        emailOtp.setEmail(normalizedEmail);
        emailOtp.setOtpHash(passwordEncoder.encode(otp));
        emailOtp.setPurpose(purpose);
        emailOtp.setExpiresAt(now.plusMinutes(OTP_EXPIRY_MINUTES));
        emailOtp.setAttemptCount(0);

        otpRepository.save(emailOtp);

        // Deliver OTP through the configured email service.
        emailService.sendOtp(normalizedEmail, otp, purpose);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean verifyOtp(String email, String otp, OtpPurpose purpose) {

        String normalizedEmail = email.trim().toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        EmailOtp emailOtp = otpRepository
                .findTopByEmailAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
                        normalizedEmail, purpose)
                .orElse(null);

        if (emailOtp == null) {
            return false;
        }

        if (emailOtp.getExpiresAt() == null ||
                !emailOtp.getExpiresAt().isAfter(now)) {
            emailOtp.setConsumedAt(now);
            return false;
        }

        if (emailOtp.getAttemptCount() >= MAX_ATTEMPTS) {
            emailOtp.setConsumedAt(now);
            return false;
        }

        emailOtp.setAttemptCount(emailOtp.getAttemptCount() + 1);

        if (!passwordEncoder.matches(otp, emailOtp.getOtpHash())) {
            if (emailOtp.getAttemptCount() >= MAX_ATTEMPTS) {
                emailOtp.setConsumedAt(now);
            }

            return false;
        }

        emailOtp.setConsumedAt(now);
        return true;
    }
}
