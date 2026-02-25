-- ============================================================
-- BillSplitter Database Setup
-- Run this in psql or pgAdmin before starting the app
-- ============================================================

-- Create database (run as superuser)
CREATE DATABASE billsplitter_db;

-- Connect to the database, then run:
-- \c billsplitter_db

-- The tables will be auto-created by Hibernate (spring.jpa.hibernate.ddl-auto=update)
-- But you can also create them manually below:

-- USERS
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  UNIQUE NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER'
);

-- EXPENSES
CREATE TABLE IF NOT EXISTS expenses (
    id             BIGSERIAL PRIMARY KEY,
    description    VARCHAR(255)   NOT NULL,
    amount         NUMERIC(10,2)  NOT NULL,
    paid_by_id     BIGINT         NOT NULL REFERENCES users(id),
    expense_date   DATE           NOT NULL,
    created_at     TIMESTAMP      NOT NULL,
    status         VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    approval_count INT            NOT NULL DEFAULT 0,
    settled_in_day BOOLEAN        NOT NULL DEFAULT FALSE
);

-- EXPENSE_PARTICIPANTS (join table)
CREATE TABLE IF NOT EXISTS expense_participants (
    expense_id BIGINT NOT NULL REFERENCES expenses(id),
    user_id    BIGINT NOT NULL REFERENCES users(id),
    PRIMARY KEY (expense_id, user_id)
);

-- EXPENSE_APPROVALS
CREATE TABLE IF NOT EXISTS expense_approvals (
    id          BIGSERIAL PRIMARY KEY,
    expense_id  BIGINT       NOT NULL REFERENCES expenses(id),
    approver_id BIGINT       NOT NULL REFERENCES users(id),
    action      VARCHAR(10)  NOT NULL,
    comment     VARCHAR(500),
    action_at   TIMESTAMP    NOT NULL,
    UNIQUE (expense_id, approver_id)
);

-- DAY_SETTLEMENTS
CREATE TABLE IF NOT EXISTS day_settlements (
    id                BIGSERIAL PRIMARY KEY,
    settlement_date   DATE       UNIQUE NOT NULL,
    concluded_at      TIMESTAMP  NOT NULL,
    concluded_by_id   BIGINT     REFERENCES users(id)
);

-- SETTLEMENT_TRANSACTIONS
CREATE TABLE IF NOT EXISTS settlement_transactions (
    id            BIGSERIAL PRIMARY KEY,
    settlement_id BIGINT         NOT NULL REFERENCES day_settlements(id),
    debtor_id     BIGINT         NOT NULL REFERENCES users(id),
    creditor_id   BIGINT         NOT NULL REFERENCES users(id),
    amount        NUMERIC(10,2)  NOT NULL
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_expenses_date   ON expenses(expense_date);
CREATE INDEX IF NOT EXISTS idx_expenses_status ON expenses(status);
CREATE INDEX IF NOT EXISTS idx_approvals_expense ON expense_approvals(expense_id);
