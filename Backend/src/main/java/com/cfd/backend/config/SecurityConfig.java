
package com.cfd.backend.config;

import com.cfd.backend.service.AppUserDetailsService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            AppUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
public AuthenticationManager authenticationManager(
        DaoAuthenticationProvider authenticationProvider) {
    return new ProviderManager(authenticationProvider);
}

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider)
            throws Exception {

        http
            .authenticationProvider(authenticationProvider)

            .csrf(csrf -> csrf
                .csrfTokenRepository(
                    CookieCsrfTokenRepository.withHttpOnlyFalse()
                )
                .csrfTokenRequestHandler(
                    new CsrfTokenRequestAttributeHandler()
                )
            )

            .authorizeHttpRequests(auth -> auth
    .requestMatchers(
        "/api/v1/auth/**",
        "/api/v1/health/ml"
    ).permitAll()

    .requestMatchers("/api/v1/admin/**")
        .hasRole("ADMIN")

    .requestMatchers("/api/v1/**")
        .hasAnyRole("ADMIN", "ANALYST")

    .anyRequest().permitAll()
)

            .exceptionHandling(exception -> exception
    .authenticationEntryPoint(
        new HttpStatusEntryPoint(
            org.springframework.http.HttpStatus.UNAUTHORIZED
        )
    )
)

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(fixation -> fixation.migrateSession())
            )

            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            .logout(logout -> logout
                .logoutUrl("/api/v1/auth/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) ->
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT)
                )
            );

        return http.build();
    }
}
