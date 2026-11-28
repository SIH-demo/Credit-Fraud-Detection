
package com.cfd.backend.service;

import com.cfd.backend.entity.AppUser;
import com.cfd.backend.enums.UserStatus;
import com.cfd.backend.repository.AppUserRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public AppUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        AppUser appUser = userRepository.findByEmail(
               email.trim().toLowerCase(Locale.ROOT)
        ).orElseThrow(() -> new UsernameNotFoundException(
                "Invalid email or password."
        ));

        boolean active = appUser.getStatus() == UserStatus.ACTIVE
                && Boolean.TRUE.equals(appUser.getEmailVerified());

        return User.withUsername(appUser.getEmail())
                .password(appUser.getPasswordHash())
                .authorities("ROLE_" + appUser.getRole().name())
                .disabled(!active)
                .build();
    }
}
