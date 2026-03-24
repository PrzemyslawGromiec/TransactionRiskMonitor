# Transaction Risk Monitoring System

## Overview

This project implements a **real-time transaction risk monitoring engine** that evaluates financial transactions and assigns a fraud risk score based on behavioural signals.

The system processes incoming transactions, analyses contextual data (account profile, velocity, location, merchant history), and produces a **risk assessment** used to detect potentially fraudulent activity.

It is designed using **Hexagonal Architecture (Ports & Adapters)** to ensure strong separation between domain logic and infrastructure.

---

## Key Features

- Real-time transaction risk scoring
- Idempotent transaction ingestion
- Behaviour-based fraud detection signals
- Dynamic query API for analysing transactions and risk assessments
- PostgreSQL persistence with JPA
- Modular, testable architecture

---

## Architecture

The system follows **Hexagonal Architecture**, separating core business logic from external concerns.

### Domain Layer

Contains all core business logic and models.

**Key concepts:**


* `Transaction`
* `AccountProfile`
* `Money`
* `Country`
* `MerchantId`
* `RiskScore`
* `RiskAssessment`
* `RiskReason`


**Core service:**


`RiskScorer`

Responsible for evaluating transactions and calculating risk scores.

---

### Application Layer

Coordinates use cases and orchestrates domain interactions.

**Main use case:**

`IngestTransactionService`


**Responsibilities:**

1. Map input → domain objects
2. Enforce idempotency
3. Retrieve contextual signals via ports
4. Execute risk scoring
5. Persist results
6. Publish alerts

---

### Ports

Define interactions between domain and infrastructure.


* `TransactionRepositoryPort`
* `AccountProfilePort`
* `VelocityPort`
* `LocationHistoryPort`
* `MerchantHistoryPort`
* `AlertPublisherPort`


---

### Adapters

Provide concrete implementations of ports.

Includes:

* `JPA repositories (PostgreSQL)`
* `In-memory adapters (for testing/demo)`
* `ConsoleAlertPublisher`


---

## Persistence

The system uses **PostgreSQL with JPA/Hibernate** for persistence.

- `transactions` table stores transaction data
- `risk_assessments` table stores evaluation results
- Unique constraint on `transaction_id` ensures idempotency
- Indexes improve query performance

---

## Query Module (Read Side)

The system includes a **dynamic query API** for analysing transactions and risk assessments.

### Features

- Flexible filtering:
    - `transactionId`
    - `accountId`
    - `riskScore range`
    - `reason`
    - `occurredAt range`
- Cross-entity querying (`transactions` + `risk_assessments`)
- DTO projection (no entity exposure)
- Criteria API-based dynamic query building

### Example Endpoint


GET /api/v1/transactions/search?reason=IMPOSSIBLE_TRAVEL&minRiskScore=40

### Architecture

Controller → Service → Validator → QueryRepository (Criteria API)

This represents a **read model**, separated from ingestion (write flow).

---

## Fraud Detection Signals

The risk engine evaluates multiple behavioural signals:

### High Transaction Amount

`amount > 5000`


### New Account
`Recently created accounts are considered higher risk.`

### High-Risk Country
`Transactions from predefined high-risk regions increase risk.`

### Transaction Velocity

`5 transactions within 5 minutes`


### Impossible Travel
`Geographically inconsistent transactions increase risk.`

### First-Time Merchant
`New merchant interactions increase risk.`

### Account Trust Status

* `FLAGGED → increases risk`
* `TRUSTED → reduces risk`


---

## Risk Scoring

Risk score range:

`0 – 100`


Threshold:

`80 → HIGH RISK`


High-risk transactions trigger alerts.

---

## Transaction Processing Flow

* `Receive transaction command`
* `Convert to domain model`
* `Check idempotency`
* `Load account profile`
* `Evaluate velocity & location signals`
* `Analyse merchant history`
* `Calculate risk score`
* `Persist transaction & assessment`
* `Publish alert (if high risk)`

---

## Example Output


* `Accepted(transactionId=tx-1, riskScore=85)`
* `Accepted(transactionId=tx-2, riskScore=90)`


---

## Testing

Includes unit tests for:


* RiskScorerTest
* IngestTransactionServiceTest


Covers:

- scoring logic
- ingestion flow
- idempotency
- alert triggering

---

## Technologies

- Java 21
- Spring Boot
- JPA / Hibernate (Criteria API)
- PostgreSQL
- JUnit 5
- Hexagonal Architecture
- Domain-Driven Design

---

## Future Improvements

- Kafka-based ingestion
- Distributed velocity tracking
- ML-based fraud detection
- Improved geolocation analysis
- Optimised storage for risk reasons (array / join table)
- Pagination and sorting in query API

---

## Purpose

This project demonstrates how to build a **scalable, modular fraud detection system** using modern backend architecture.

Key principles:

- separation of concerns
- domain-driven design
- testability
- extensibility
- production-ready structure  