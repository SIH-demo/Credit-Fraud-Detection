-- ============================================================
-- Credit Card Fraud Detection
-- Flyway Migration V1
-- MySQL 8.0+
-- All DATETIME values are stored in UTC.
-- ============================================================

-- 1. Original transactions
CREATE TABLE transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    transaction_reference VARCHAR(100) NOT NULL,
    external_reference VARCHAR(100) NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency CHAR(3) NOT NULL,
    transaction_time DATETIME(6) NOT NULL,
    merchant_name VARCHAR(255) NOT NULL,
    merchant_id VARCHAR(100) NOT NULL,
    step INT NOT NULL,
    age VARCHAR(2) NOT NULL,
    gender VARCHAR(2) NOT NULL,
    category VARCHAR(60) NOT NULL,
    country VARCHAR(100) NOT NULL,
    masked_card_last4 CHAR(4) NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    INDEX idx_transactions_reference (transaction_reference),
    INDEX idx_transactions_external_reference (external_reference),
    INDEX idx_transactions_transaction_time (transaction_time),
    INDEX idx_transactions_status (status),

    CONSTRAINT chk_transactions_amount
        CHECK (amount > 0),
    CONSTRAINT chk_transactions_step
        CHECK (step BETWEEN 0 AND 179)
) ENGINE=InnoDB;


-- 2. Predictions
CREATE TABLE predictions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    prediction TINYINT NOT NULL,
    risk_score DECIMAL(8,6) NOT NULL,
    risk_level VARCHAR(10) NOT NULL,
    model_version VARCHAR(50) NOT NULL,
    threshold_used DECIMAL(8,6) NOT NULL,
    correlation_id VARCHAR(36) NOT NULL,
    predicted_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uk_predictions_correlation_id (correlation_id),
    INDEX idx_predictions_transaction_id (transaction_id),
    INDEX idx_predictions_risk_level (risk_level),
    INDEX idx_predictions_predicted_at (predicted_at),
    INDEX idx_predictions_model_version (model_version),

    CONSTRAINT fk_predictions_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT chk_predictions_prediction
        CHECK (prediction IN (0, 1)),
    CONSTRAINT chk_predictions_risk_score
        CHECK (risk_score BETWEEN 0 AND 1),
    CONSTRAINT chk_predictions_threshold
        CHECK (threshold_used BETWEEN 0 AND 1),
    CONSTRAINT chk_predictions_risk_level
        CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH'))
) ENGINE=InnoDB;


-- 3. SHAP prediction explanations
CREATE TABLE prediction_explanations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    prediction_id BIGINT NOT NULL,
    feature_name VARCHAR(100) NOT NULL,
    contribution DOUBLE NOT NULL,
    impact VARCHAR(30) NOT NULL,
    feature_rank SMALLINT NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_explanation_feature_rank
        (prediction_id, feature_rank),
    INDEX idx_explanations_prediction_id (prediction_id),

    CONSTRAINT fk_explanations_prediction
        FOREIGN KEY (prediction_id)
        REFERENCES predictions(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT chk_explanations_feature_rank
        CHECK (feature_rank > 0)
) ENGINE=InnoDB;


-- 4. Batch jobs
CREATE TABLE batch_jobs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    batch_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_rows INT NOT NULL DEFAULT 0,
    processed_rows INT NOT NULL DEFAULT 0,
    error_count INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    completed_at DATETIME(6) NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_batch_jobs_batch_id (batch_id),
    INDEX idx_batch_jobs_status (status),
    INDEX idx_batch_jobs_created_at (created_at),

    CONSTRAINT chk_batch_jobs_status
        CHECK (status IN (
            'RECEIVED', 'PROCESSING', 'COMPLETED',
            'PARTIAL', 'FAILED'
        )),
    CONSTRAINT chk_batch_jobs_counts
        CHECK (
            total_rows >= 0
            AND processed_rows >= 0
            AND error_count >= 0
            AND processed_rows + error_count <= total_rows
        )
) ENGINE=InnoDB;


-- 5. Individual batch rows
CREATE TABLE batch_job_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    batch_job_id BIGINT NOT NULL,
    transaction_id BIGINT NULL,
    prediction_id BIGINT NULL,
    source_row_number INT NOT NULL,
    processing_status VARCHAR(20) NOT NULL,
    error_message TEXT NULL,
    raw_payload JSON NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uk_batch_job_row (batch_job_id, source_row_number),
    INDEX idx_batch_items_transaction_id (transaction_id),
    INDEX idx_batch_items_prediction_id (prediction_id),
    INDEX idx_batch_items_status (processing_status),

    CONSTRAINT fk_batch_items_job
        FOREIGN KEY (batch_job_id)
        REFERENCES batch_jobs(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_batch_items_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_batch_items_prediction
        FOREIGN KEY (prediction_id)
        REFERENCES predictions(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT chk_batch_items_source_row_number
        CHECK (source_row_number > 0),
    CONSTRAINT chk_batch_items_status
        CHECK (processing_status IN (
            'PENDING', 'PROCESSING', 'COMPLETED','SKIPPED', 'FAILED'
        ))
) ENGINE=InnoDB;


-- 6. Model evaluation runs
CREATE TABLE model_evaluations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    model_name VARCHAR(100) NULL,
    model_version VARCHAR(50) NULL,
    threshold DECIMAL(8,6) NOT NULL,
    evaluation_count BIGINT NOT NULL,
    accuracy DECIMAL(10,8) NULL,
    precision_score DECIMAL(10,8) NULL,
    recall_score DECIMAL(10,8) NULL,
    f1_score DECIMAL(10,8) NULL,
    roc_auc DECIMAL(10,8) NULL,
    average_precision DECIMAL(10,8) NULL,
    score_status VARCHAR(50) NULL,
    true_positive BIGINT NULL,
    true_negative BIGINT NULL,
    false_positive BIGINT NULL,
    false_negative BIGINT NULL,
    evaluated_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    INDEX idx_model_evaluations_version (model_version),
    INDEX idx_model_evaluations_evaluated_at (evaluated_at),

    CONSTRAINT chk_model_evaluations_threshold
        CHECK (threshold BETWEEN 0 AND 1),
    CONSTRAINT chk_model_evaluations_count
        CHECK (evaluation_count > 0),
    CONSTRAINT chk_model_evaluations_accuracy
        CHECK (accuracy IS NULL OR accuracy BETWEEN 0 AND 1),
    CONSTRAINT chk_model_evaluations_precision
        CHECK (precision_score IS NULL OR precision_score BETWEEN 0 AND 1),
    CONSTRAINT chk_model_evaluations_recall
        CHECK (recall_score IS NULL OR recall_score BETWEEN 0 AND 1),
    CONSTRAINT chk_model_evaluations_f1
        CHECK (f1_score IS NULL OR f1_score BETWEEN 0 AND 1),
    CONSTRAINT chk_model_evaluations_roc_auc
        CHECK (roc_auc IS NULL OR roc_auc BETWEEN 0 AND 1),
    CONSTRAINT chk_model_evaluations_average_precision
        CHECK (
            average_precision IS NULL
            OR average_precision BETWEEN 0 AND 1
        )
) ENGINE=InnoDB;


-- 7. Individual model evaluation records
CREATE TABLE model_evaluation_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    evaluation_id BIGINT NOT NULL,
    record_reference VARCHAR(100) NOT NULL,
    transaction_id BIGINT NULL,
    actual_fraud TINYINT NOT NULL,
    predicted_fraud TINYINT NOT NULL,
    risk_score DECIMAL(8,6) NULL,

    PRIMARY KEY (id),
    INDEX idx_evaluation_records_evaluation_id (evaluation_id),
    INDEX idx_evaluation_records_transaction_id (transaction_id),

    CONSTRAINT fk_evaluation_records_evaluation
        FOREIGN KEY (evaluation_id)
        REFERENCES model_evaluations(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_evaluation_records_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT chk_evaluation_records_actual
        CHECK (actual_fraud IN (0, 1)),
    CONSTRAINT chk_evaluation_records_predicted
        CHECK (predicted_fraud IN (0, 1)),
    CONSTRAINT chk_evaluation_records_risk_score
        CHECK (
            risk_score IS NULL
            OR risk_score BETWEEN 0 AND 1
        )
) ENGINE=InnoDB;


-- ============================================================
-- 8. Investigations
-- Automatically created for HIGH-risk predictions.
-- One investigation per transaction.
-- ============================================================

CREATE TABLE investigations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    prediction_id BIGINT NOT NULL,
    priority VARCHAR(10) NOT NULL DEFAULT 'HIGH',
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    assigned_to VARCHAR(100) NULL,
    outcome VARCHAR(30) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uk_investigations_transaction_id (transaction_id),
    UNIQUE KEY uk_investigations_prediction_id (prediction_id),
    INDEX idx_investigations_status (status),
    INDEX idx_investigations_priority (priority),
    INDEX idx_investigations_created_at (created_at),

    CONSTRAINT fk_investigations_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_investigations_prediction
        FOREIGN KEY (prediction_id)
        REFERENCES predictions(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT chk_investigations_priority
        CHECK (priority IN ('HIGH', 'MEDIUM', 'LOW')),

    CONSTRAINT chk_investigations_status
        CHECK (status IN (
            'OPEN', 'IN_PROGRESS', 'UNDER_REVIEW', 'CLOSED'
        )),

    CONSTRAINT chk_investigations_outcome
        CHECK (
            outcome IS NULL
            OR outcome IN (
                'CONFIRMED_FRAUD', 'LEGITIMATE', 'INCONCLUSIVE'
            )
        )
) ENGINE=InnoDB;


-- ============================================================
-- 9. Investigation notes
-- ============================================================

CREATE TABLE investigation_notes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    investigation_id BIGINT NOT NULL,
    author VARCHAR(100) NULL,
    note TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    INDEX idx_investigation_notes_investigation_id (investigation_id),

    CONSTRAINT fk_investigation_notes_investigation
        FOREIGN KEY (investigation_id)
        REFERENCES investigations(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;


-- ============================================================
-- 10. Investigation history
-- ============================================================

CREATE TABLE investigation_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    investigation_id BIGINT NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    changed_by VARCHAR(100) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    INDEX idx_investigation_history_investigation_id (investigation_id),
    INDEX idx_investigation_history_created_at (created_at),

    CONSTRAINT fk_investigation_history_investigation
        FOREIGN KEY (investigation_id)
        REFERENCES investigations(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT chk_investigation_history_event_type
        CHECK (event_type IN (
            'CASE_CREATED',
            'ASSIGNED',
            'STATUS_CHANGED',
            'OUTCOME_UPDATED',
            'NOTE_ADDED'
        ))
) ENGINE=InnoDB;



-- ============================================================
-- 11. Application users
-- ============================================================

CREATE TABLE app_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ANALYST',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uk_app_users_email (email),
    INDEX idx_app_users_status (status),
    INDEX idx_app_users_role (role),

    CONSTRAINT chk_app_users_role
        CHECK (role IN ('ADMIN', 'ANALYST')),

    CONSTRAINT chk_app_users_status
        CHECK (status IN ('PENDING', 'ACTIVE', 'LOCKED'))
) ENGINE=InnoDB;


-- ============================================================
-- 12. Email OTP records
-- ============================================================

CREATE TABLE email_otps (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NULL,
    email VARCHAR(254) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    purpose VARCHAR(30) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    consumed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    INDEX idx_email_otps_email_purpose (email, purpose),
    INDEX idx_email_otps_user_id (user_id),
    INDEX idx_email_otps_expires_at (expires_at),

    CONSTRAINT fk_email_otps_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT chk_email_otps_purpose
        CHECK (purpose IN ('REGISTRATION', 'PASSWORD_RESET')),

    CONSTRAINT chk_email_otps_attempt_count
        CHECK (attempt_count >= 0)
) ENGINE=InnoDB;
