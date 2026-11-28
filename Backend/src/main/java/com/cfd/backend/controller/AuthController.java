
package com.cfd.backend.controller;

import com.cfd.backend.dto.auth.AuthMessageResponse;
import com.cfd.backend.dto.auth.AuthUserResponse;
import com.cfd.backend.dto.auth.ForgotPasswordRequest;
import com.cfd.backend.dto.auth.LoginRequest;
import com.cfd.backend.dto.auth.RegisterRequest;
import com.cfd.backend.dto.auth.ResetPasswordRequest;
import com.cfd.backend.dto.auth.VerifyOtpRequest;
import com.cfd.backend.entity.AppUser;
import com.cfd.backend.repository.AppUserRepository;
import com.cfd.backend.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final AppUserRepository userRepository;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AuthService authService,
            AuthenticationManager authenticationManager,
            AppUserRepository userRepository) {

        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;

        this.securityContextRepository =
                new HttpSessionSecurityContextRepository();
    }

    @GetMapping("/csrf")
public ResponseEntity<Map<String, String>> getCsrfToken(
        CsrfToken csrfToken) {

    return ResponseEntity.ok(
            Map.of("token", csrfToken.getToken())
    );
}

    @PostMapping("/register")
    public ResponseEntity<AuthMessageResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthMessageResponse> verifyRegistrationOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        return ResponseEntity.ok(
                authService.verifyRegistrationOtp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String email = request.email().trim().toLowerCase(Locale.ROOT);

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    request.password()
                            )
                    );

            // Rotate an existing session ID to reduce session fixation risk.
            if (httpRequest.getSession(false) != null) {
                httpRequest.changeSessionId();
            }

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // Persist the authenticated context in the HTTP session.
            securityContextRepository.saveContext(
                    context,
                    httpRequest,
                    httpResponse
            );

            AppUser user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException(
                            "Authenticated user was not found."));

            return ResponseEntity.ok(
                    new AuthUserResponse(
                            user.getId(),
                            user.getFullName(),
                            user.getEmail(),
                            user.getRole().name()
                    )
            );

        } catch (AuthenticationException exception) {
            SecurityContextHolder.clearContext();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthMessageResponse(
                            "Invalid email or password, or account not verified."));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthMessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        return ResponseEntity.ok(
                authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthMessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        return ResponseEntity.ok(
                authService.resetPassword(request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthMessageResponse> handleIllegalArgument(
            IllegalArgumentException exception) {

        return ResponseEntity.badRequest()
                .body(new AuthMessageResponse(exception.getMessage()));
    }
}
