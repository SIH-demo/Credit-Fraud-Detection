
package com.cfd.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cfd.backend.entity.Investigation;
import com.cfd.backend.entity.InvestigationHistory;
import com.cfd.backend.entity.Prediction;
import com.cfd.backend.enums.InvestigationEventType;
import com.cfd.backend.enums.InvestigationStatus;
import com.cfd.backend.enums.RiskLevel;
import com.cfd.backend.repository.InvestigationHistoryRepository;
import com.cfd.backend.repository.InvestigationRepository;

@Service
public class InvestigationService {

    private final InvestigationRepository investigationRepository;
    private final InvestigationHistoryRepository historyRepository;

    public InvestigationService(
            InvestigationRepository investigationRepository,
            InvestigationHistoryRepository historyRepository
    ) {
        this.investigationRepository = investigationRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public void createIfRequired(Prediction prediction) {

        if (prediction.getRiskLevel() != RiskLevel.HIGH) {
            return;
        }

        Long transactionId = prediction.getTransaction().getId();

        if (investigationRepository.existsByTransactionId(transactionId)) {
            return;
        }

        Investigation investigation = new Investigation();

        investigation.setTransaction(prediction.getTransaction());
        investigation.setPrediction(prediction);
        investigation.setPriority(RiskLevel.HIGH);
        investigation.setStatus(InvestigationStatus.OPEN);

        Investigation savedInvestigation =
                investigationRepository.save(investigation);

        InvestigationHistory history = new InvestigationHistory();

        history.setInvestigation(savedInvestigation);
        history.setEventType(InvestigationEventType.CASE_CREATED);
        history.setDescription(
                "Investigation automatically created for HIGH-risk prediction."
        );

        historyRepository.save(history);
    }
}
