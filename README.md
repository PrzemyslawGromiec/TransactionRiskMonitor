# Transaction Risk Monitoring System

## Overview

This project implements a **real-time transaction risk monitoring engine** designed to analyse financial transactions and assign a fraud risk score based on behavioural signals.

The system evaluates incoming transactions using a set of domain rules such as transaction amount, account status, transaction velocity, geographic anomalies, and merchant history.

The application follows **Hexagonal Architecture (Ports and Adapters)** to keep the core domain logic independent from infrastructure such as databases, APIs, or messaging systems.

This architecture allows the risk engine to remain easily testable, extensible, and adaptable to different runtime environments.

---

# Architecture

The system is structured into four main layers.

### Domain Layer

The **domain layer** contains the core business logic and rules for evaluating transaction risk.

Key domain concepts include:

* `Transaction`
* `AccountProfile`
* `Money`
* `Country`
* `MerchantId`
* `RiskScore`
* `RiskAssessment`
* `RiskReason`

The central domain service is:

```
RiskScorer
```

which calculates a risk score based on behavioural signals.

---

### Application Layer

The application layer orchestrates the use case of ingesting and evaluating transactions.

Main use case:

```
IngestTransactionService
```

Responsibilities:

1. Convert incoming command data into domain objects
2. Perform idempotency checks
3. Retrieve behavioural context via outbound ports
4. Invoke the domain risk scoring engine
5. Persist results
6. Publish alerts for high-risk transactions

Inbound command:

```
IngestTransactionCommand
```

Result types:

```
Accepted
Duplicated
```

---

### Ports

Ports define how the domain interacts with the outside world.

Outbound ports:

```
TransactionRepositoryPort
AccountProfilePort
VelocityPort
LocationHistoryPort
MerchantHistoryPort
AlertPublisherPort
```

These ports provide the contextual data required to evaluate risk.

---

### Adapters

Adapters implement the ports and connect the application to infrastructure.

Current implementations include:

```
InMemoryTransactionRepository
InMemoryAccountProfileAdapter
InMemoryVelocityAdapter
InMemoryLocationHistoryAdapter
InMemoryMerchantHistoryAdapter
ConsoleAlertPublisher
```

These adapters simulate infrastructure for testing and demonstration purposes.

---

# Fraud Detection Signals

The risk engine currently evaluates the following signals.

### High Transaction Amount

Transactions above a defined threshold increase risk.

```
amount > 5000
```

---

### New Account

Accounts recently created are treated as higher risk.

---

### High-Risk Country

Transactions originating from predefined high-risk countries increase the score.

---

### Transaction Velocity

Multiple transactions within a short time window indicate suspicious behaviour.

Example rule:

```
>= 5 transactions within 5 minutes
```

---

### Impossible Travel

If a transaction occurs in a location that is geographically impossible given recent activity, risk increases.

---

### First-Time Merchant

Transactions with merchants never previously used by the account increase risk.

---

### Account Trust Status

Accounts marked as:

```
FLAGGED
```

receive additional risk.

Accounts marked as:

```
TRUSTED
```

receive reduced risk.

---

# Risk Score

The system generates a risk score between:

```
0 – 100
```

Transactions exceeding the threshold:

```
>= 80
```

are considered **high risk** and trigger an alert.

---

# Transaction Processing Flow

When a transaction is ingested the following steps occur:

1. A transaction command enters the system.
2. The use case converts raw input into domain objects.
3. The system checks if the transaction has already been processed (idempotency).
4. Account profile data is retrieved.
5. Transaction velocity statistics are retrieved.
6. Recent location history is analysed.
7. Merchant history is checked.
8. The domain risk engine calculates a risk score.
9. The transaction and score are stored.
10. If the score exceeds the threshold, an alert is published.

---

# Running the Example

The project includes a small demonstration runner:

```
AppRunner
```

It simulates a stream of transactions and prints the resulting risk evaluations.

Example output:

```
Accepted(transactionId=tx-1, riskScore=85)
Accepted(transactionId=tx-2, riskScore=90)
Accepted(transactionId=tx-3, riskScore=88)
```

High-risk transactions will also produce alerts.

---

# Testing

The project includes unit tests covering:

* Risk scoring logic
* Transaction ingestion workflow
* Idempotency guarantees
* Alert publishing behaviour

Example test classes:

```
RiskScorerTest
IngestTransactionServiceTest
```

---

# Technologies

The system is implemented using:

* Java 21
* JUnit 5
* Hexagonal Architecture
* Domain-Driven Design principles

---

# Future Improvements

Possible extensions include:

* Real database persistence
* Kafka transaction ingestion
* Machine learning fraud models
* Merchant category risk signals
* Geolocation distance calculations
* Distributed velocity tracking

---

# Purpose

This project demonstrates how a **modular and extensible fraud detection engine** can be implemented using modern software architecture principles.

The design emphasises:

* separation of concerns
* domain-centric modelling
* testability
* infrastructure independence
