
package com.cfd.backend.repository;

import com.cfd.backend.entity.Investigation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvestigationRepository
        extends JpaRepository<Investigation, Long> {

    boolean existsByTransactionId(Long transactionId);

    Optional<Investigation> findByTransactionId(Long transactionId);
}
