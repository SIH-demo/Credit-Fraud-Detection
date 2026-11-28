
package com.cfd.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cfd.backend.dto.InvestigationApiResponse;
import com.cfd.backend.entity.Investigation;
import com.cfd.backend.entity.InvestigationHistory;
import com.cfd.backend.entity.InvestigationNote;
import com.cfd.backend.entity.Transaction;
import com.cfd.backend.entity.Prediction;
import com.cfd.backend.enums.InvestigationEventType;
import com.cfd.backend.enums.InvestigationOutcome;
import com.cfd.backend.enums.InvestigationStatus;
import com.cfd.backend.repository.InvestigationRepository;
import com.cfd.backend.repository.InvestigationNoteRepository;
import com.cfd.backend.repository.InvestigationHistoryRepository;

@Service
public class InvestigationManagementService {

    private final InvestigationRepository investigationRepository;
    private final InvestigationNoteRepository noteRepository;
    private final InvestigationHistoryRepository historyRepository;

    public InvestigationManagementService(
            InvestigationRepository investigationRepository,
            InvestigationNoteRepository noteRepository,
            InvestigationHistoryRepository historyRepository
    ) {
        this.investigationRepository = investigationRepository;
        this.noteRepository = noteRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public InvestigationApiResponse<List<InvestigationApiResponse.InvestigationSummary>>
            getAllInvestigations() {

        List<InvestigationApiResponse.InvestigationSummary> investigations =
                investigationRepository.findAll()
                        .stream()
                        .sorted((a, b) ->
                                b.getCreatedAt().compareTo(a.getCreatedAt()))
                        .map(this::toSummary)
                        .toList();

        return new InvestigationApiResponse<>(investigations);
    }

    @Transactional(readOnly = true)
    public InvestigationApiResponse<InvestigationApiResponse.InvestigationDetails>
            getInvestigationDetails(Long id) {

        Investigation investigation = getInvestigation(id);

        Transaction transaction = investigation.getTransaction();
        Prediction prediction = investigation.getPrediction();

        List<InvestigationApiResponse.NoteDetails> notes =
                noteRepository
                        .findByInvestigationIdOrderByCreatedAtDesc(id)
                        .stream()
                        .map(note -> new InvestigationApiResponse.NoteDetails(
                                note.getId(),
                                note.getAuthor(),
                                note.getNote(),
                                format(note.getCreatedAt())
                        ))
                        .toList();

        List<InvestigationApiResponse.HistoryDetails> history =
                historyRepository
                        .findByInvestigationIdOrderByCreatedAtDesc(id)
                        .stream()
                        .map(event -> new InvestigationApiResponse.HistoryDetails(
                                event.getId(),
                                event.getEventType().name(),
                                event.getDescription(),
                                event.getChangedBy(),
                                format(event.getCreatedAt())
                        ))
                        .toList();

        var transactionDetails =
                new InvestigationApiResponse.TransactionDetails(
                        transaction.getId(),
                        transaction.getTransactionReference(),
                        transaction.getExternalReference(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        format(transaction.getTransactionTime()),
                        transaction.getMerchantName(),
                        transaction.getMerchantId(),
                        transaction.getCountry(),
                        transaction.getMaskedCardLast4(),
                        transaction.getStatus()
                );

        var predictionDetails =
                new InvestigationApiResponse.PredictionDetails(
                        prediction.getId(),
                        prediction.getPrediction(),
                        prediction.getRiskScore(),
                        prediction.getRiskLevel().name(),
                        prediction.getModelVersion(),
                        format(prediction.getPredictedAt())
                );

        var details =
                new InvestigationApiResponse.InvestigationDetails(
                        investigation.getId(),
                        caseId(investigation.getId()),
                        investigation.getStatus().name(),
                        investigation.getPriority().name(),
                        investigation.getAssignedTo(),
                        investigation.getOutcome() == null
                                ? null : investigation.getOutcome().name(),
                        format(investigation.getCreatedAt()),
                        format(investigation.getUpdatedAt()),
                        transactionDetails,
                        predictionDetails,
                        notes,
                        history
                );

        return new InvestigationApiResponse<>(details);
    }

    @Transactional
    public InvestigationApiResponse<InvestigationApiResponse.InvestigationDetails>
            updateInvestigation(
                    Long id,
                    InvestigationApiResponse.UpdateRequest request
            ) {

        if (request == null) {
            throw new IllegalArgumentException("Update request is required.");
        }

        Investigation investigation = getInvestigation(id);

        String changedBy = clean(request.changedBy());

        String assignedTo = clean(request.assignedTo());

        if (assignedTo != null &&
                !assignedTo.equals(investigation.getAssignedTo())) {

            investigation.setAssignedTo(assignedTo);

            saveHistory(
                    investigation,
                    InvestigationEventType.ASSIGNED,
                    "Case assigned to " + assignedTo + ".",
                    changedBy
            );
        }

        if (request.status() != null && !request.status().isBlank()) {

            InvestigationStatus newStatus;

            try {
                newStatus = InvestigationStatus.valueOf(
                        request.status().trim().toUpperCase()
                );
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "Invalid investigation status."
                );
            }

            if (newStatus != investigation.getStatus()) {

                InvestigationStatus oldStatus = investigation.getStatus();

                investigation.setStatus(newStatus);

                saveHistory(
                        investigation,
                        InvestigationEventType.STATUS_CHANGED,
                        "Status changed from " + oldStatus +
                                " to " + newStatus + ".",
                        changedBy
                );
            }
        }

        if (request.outcome() != null && !request.outcome().isBlank()) {

            InvestigationOutcome newOutcome;

            try {
                newOutcome = InvestigationOutcome.valueOf(
                        request.outcome().trim().toUpperCase()
                );
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "Invalid investigation outcome."
                );
            }

            if (newOutcome != investigation.getOutcome()) {

                InvestigationOutcome oldOutcome =
                        investigation.getOutcome();

                investigation.setOutcome(newOutcome);

                saveHistory(
                        investigation,
                        InvestigationEventType.OUTCOME_UPDATED,
                        "Outcome changed from " +
                                (oldOutcome == null ? "NONE" : oldOutcome) +
                                " to " + newOutcome + ".",
                        changedBy
                );
            }
        }

        String remarks = clean(request.remarks());

        if (remarks != null) {

            InvestigationNote note = new InvestigationNote();
            note.setInvestigation(investigation);
            note.setAuthor(changedBy);
            note.setNote(remarks);

            noteRepository.save(note);

            saveHistory(
                    investigation,
                    InvestigationEventType.NOTE_ADDED,
                    "Investigation note added.",
                    changedBy
            );
        }

        investigationRepository.save(investigation);

        return getInvestigationDetails(id);
    }

    private Investigation getInvestigation(Long id) {

        return investigationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Investigation not found: " + id
                        ));
    }

    private InvestigationApiResponse.InvestigationSummary toSummary(
            Investigation investigation
    ) {

        Transaction transaction = investigation.getTransaction();
        Prediction prediction = investigation.getPrediction();

        return new InvestigationApiResponse.InvestigationSummary(
                investigation.getId(),
                caseId(investigation.getId()),
                transaction.getId(),
                transaction.getTransactionReference(),
                transaction.getAmount(),
                transaction.getCurrency(),
                prediction.getRiskScore(),
                prediction.getRiskLevel().name(),
                investigation.getPriority().name(),
                investigation.getAssignedTo(),
                investigation.getStatus().name(),
                format(investigation.getCreatedAt())
        );
    }

    private void saveHistory(
            Investigation investigation,
            InvestigationEventType eventType,
            String description,
            String changedBy
    ) {

        InvestigationHistory history = new InvestigationHistory();

        history.setInvestigation(investigation);
        history.setEventType(eventType);
        history.setDescription(description);
        history.setChangedBy(changedBy);

        historyRepository.save(history);
    }

    private String caseId(Long id) {
        return "INV-" + String.format("%06d", id);
    }

    private String clean(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String format(LocalDateTime value) {
        return value == null ? null : value.toString();
    }
}
