
package com.cfd.backend.entity;

import com.cfd.backend.enums.InvestigationOutcome;
import com.cfd.backend.enums.InvestigationStatus;
import com.cfd.backend.enums.RiskLevel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "investigations",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_investigations_transaction_id",
            columnNames = "transaction_id"
        ),
        @UniqueConstraint(
            name = "uk_investigations_prediction_id",
            columnNames = "prediction_id"
        )
    }
)
public class Investigation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prediction_id", nullable = false)
    private Prediction prediction;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private RiskLevel priority = RiskLevel.HIGH;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvestigationStatus status = InvestigationStatus.OPEN;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 30)
    private InvestigationOutcome outcome;

    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false,
        columnDefinition = "DATETIME(6)"
    )
    private LocalDateTime createdAt;

    @Column(
        name = "updated_at",
        nullable = false,
        insertable = false,
        updatable = false,
        columnDefinition = "DATETIME(6)"
    )
    private LocalDateTime updatedAt;

    public Investigation() {
    }

    public Long getId() {
        return id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Prediction getPrediction() {
        return prediction;
    }

    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }

    public RiskLevel getPriority() {
        return priority;
    }

    public void setPriority(RiskLevel priority) {
        this.priority = priority;
    }

    public InvestigationStatus getStatus() {
        return status;
    }

    public void setStatus(InvestigationStatus status) {
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public InvestigationOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(InvestigationOutcome outcome) {
        this.outcome = outcome;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
