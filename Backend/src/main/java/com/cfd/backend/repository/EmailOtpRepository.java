
package com.cfd.backend.repository;

import com.cfd.backend.entity.EmailOtp;
import com.cfd.backend.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findTopByEmailAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
        String email,
        OtpPurpose purpose
    );

    List<EmailOtp> findByEmailAndPurposeAndConsumedAtIsNull(
        String email,
        OtpPurpose purpose
    );

    Optional<EmailOtp> findTopByEmailAndPurposeOrderByCreatedAtDesc(
        String email,
        OtpPurpose purpose
    );
}
