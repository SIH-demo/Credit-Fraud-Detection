
package com.cfd.backend.service;

import com.cfd.backend.dto.auth.AuthMessageResponse;
import com.cfd.backend.dto.auth.RegisterRequest;
import com.cfd.backend.dto.auth.VerifyOtpRequest;
import com.cfd.backend.entity.AppUser;
import com.cfd.backend.enums.OtpPurpose;
import com.cfd.backend.enums.UserRole;
import com.cfd.backend.enums.UserStatus;
import com.cfd.backend.repository.AppUserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cfd.backend.dto.auth.LoginRequest;
import com.cfd.backend.dto.auth.AuthUserResponse;
import com.cfd.backend.dto.auth.ForgotPasswordRequest;
import com.cfd.backend.dto.auth.ResetPasswordRequest;

import java.util.Locale;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public AuthService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    @Transactional
    public AuthMessageResponse register(RegisterRequest request) {

        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists.");
        }

        AppUser user = new AppUser();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        // Public registration must never create administrators.
        user.setRole(UserRole.ANALYST);
        user.setStatus(UserStatus.PENDING);
        user.setEmailVerified(false);

        userRepository.save(user);

        otpService.sendOtp(email, OtpPurpose.REGISTRATION, user);

        return new AuthMessageResponse(
                "Registration started. Please check your email for the verification code.");
    }

    @Transactional
    public AuthMessageResponse verifyRegistrationOtp(
            VerifyOtpRequest request) {

        String email = normalizeEmail(request.email());

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid or expired verification code."));

        if (user.getStatus() != UserStatus.PENDING ||
                Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalArgumentException(
                    "Invalid or expired verification code.");
        }

        boolean verified = otpService.verifyOtp(
                email,
                request.otp(),
                OtpPurpose.REGISTRATION);

        if (!verified) {
            throw new IllegalArgumentException(
                    "Invalid or expired verification code.");
        }

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        return new AuthMessageResponse(
                "Email verified successfully. You can now log in.");
    }


@Transactional(readOnly = true)
public AuthUserResponse login(LoginRequest request) {

    String email = normalizeEmail(request.email());

    AppUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Invalid email or password."));

    if (user.getStatus() != UserStatus.ACTIVE ||
            !Boolean.TRUE.equals(user.getEmailVerified())) {
        throw new IllegalArgumentException(
                "Please verify your email before logging in.");
    }

    if (!passwordEncoder.matches(
            request.password(), user.getPasswordHash())) {
        throw new IllegalArgumentException(
                "Invalid email or password.");
    }

    return new AuthUserResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole().name()
    );
}


@Transactional
public AuthMessageResponse forgotPassword(ForgotPasswordRequest request) {

    String email = normalizeEmail(request.email());

    AppUser user = userRepository.findByEmail(email).orElse(null);

    // Always return the same message to avoid revealing account existence.
    if (user != null &&
            user.getStatus() == UserStatus.ACTIVE &&
            Boolean.TRUE.equals(user.getEmailVerified())) {

       try {
    otpService.sendOtp(email, OtpPurpose.PASSWORD_RESET, user);
} catch (IllegalStateException exception) {
    // Keep the response generic during the resend cooldown.
}
    }

    return new AuthMessageResponse(
            "If an eligible account exists, a password reset code has been sent to its email address.");
}

@Transactional
public AuthMessageResponse resetPassword(ResetPasswordRequest request) {

    String email = normalizeEmail(request.email());

    AppUser user = userRepository.findByEmail(email)
            .orElse(null);

    if (user == null ||
            user.getStatus() != UserStatus.ACTIVE ||
            !Boolean.TRUE.equals(user.getEmailVerified())) {
        throw new IllegalArgumentException(
                "Invalid or expired password reset code.");
    }

    boolean verified = otpService.verifyOtp(
            email,
            request.otp(),
            OtpPurpose.PASSWORD_RESET);

    if (!verified) {
        throw new IllegalArgumentException(
                "Invalid or expired password reset code.");
    }

    user.setPasswordHash(passwordEncoder.encode(request.newPassword()));

    userRepository.save(user);

    return new AuthMessageResponse(
            "Password reset successfully. Please log in with your new password.");
}


    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
