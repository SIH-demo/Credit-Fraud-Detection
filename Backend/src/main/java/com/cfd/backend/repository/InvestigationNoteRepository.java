
package com.cfd.backend.repository;

import com.cfd.backend.entity.InvestigationNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestigationNoteRepository
        extends JpaRepository<InvestigationNote, Long> {

    List<InvestigationNote> findByInvestigationIdOrderByCreatedAtDesc(
            Long investigationId
    );
}
