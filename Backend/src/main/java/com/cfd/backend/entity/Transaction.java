
package com.cfd.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_reference", nullable = false, length = 100)
    private String transactionReference;

    @Column(name = "external_reference", length = 100)
    private String externalReference;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, columnDefinition = "CHAR(3)")
    private String currency;

    @Column(name = "transaction_time", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime transactionTime;

    @Column(name = "merchant_name", nullable = false, length = 255)
    private String merchantName;

    @Column(name = "merchant_id", nullable = false, length = 100)
    private String merchantId;

    @Column(name = "step", nullable = false)
    private Integer step;

    @Column(name = "age", nullable = false, length = 2)
    private String age;

    @Column(name = "gender", nullable = false, length = 2)
    private String gender;

    @Column(name = "category", nullable = false, length = 60)
    private String category;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "masked_card_last4", columnDefinition = "CHAR(4)")
    private String maskedCardLast4;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false,
        columnDefinition = "DATETIME(6)"
    )
    private LocalDateTime createdAt;

    public Transaction() {
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getMaskedCardLast4() {
        return maskedCardLast4;
    }

    public void setMaskedCardLast4(String maskedCardLast4) {
        this.maskedCardLast4 = maskedCardLast4;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
