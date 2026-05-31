<<<<<<< HEAD
# Warehouse-Inventory-Reservation
=======
# Warehouse Inventory Reservation System

Challenge 1 implementation — a concurrent-safe REST API for reserving warehouse inventory.

## 1. Challenge Choice

**Challenge 1 — Warehouse Inventory Reservation System**

This challenge focuses on concurrency control, transactional consistency, and stateful domain modelling. It maps well to classic backend engineering problems (inventory overselling, pessimistic locking, lifecycle management) and demonstrates clean architecture without unnecessary UI complexity.

## 2. Architecture Overview

The project follows a **layered clean architecture**:

```
com.warehouse
├── api/                    # REST controllers, global exception handling
├── application/            # Use cases, DTOs, repository ports (interfaces)
├── domain/                 # Business rules, state pattern, factory, domain models
└── infrastructure/         # JPA entities, Spring Data repos, adapters
```

**Request flow:**

```
HTTP Request → Controller → ReservationService → Repository Port → JPA Adapter → PostgreSQL
```

- **Domain layer** holds pure business logic (`Reservation`, `Inventory`, state transitions).
- **Application layer** orchestrates use cases and depends only on ports (interfaces), not JPA.
- **Infrastructure layer** implements ports and maps between JPA entities and domain models.
- **API layer** handles HTTP concerns and translates exceptions to structured error responses.

This separation keeps business rules testable without a Spring context and allows swapping persistence without touching domain code.

## 3. Design Patterns

### State Pattern — Reservation lifecycle

Encapsulates valid transitions for `PENDING → CONFIRMED` and `PENDING → CANCELLED`.

| Location | Role |
|----------|------|
| `domain/state/ReservationState.java` | Interface for state behaviour |
| `domain/state/PendingReservationState.java` | Allows confirm and cancel |
| `domain/state/ConfirmedReservationState.java` | Rejects all transitions (terminal) |
| `domain/state/CancelledReservationState.java` | Rejects all transitions (terminal) |
| `domain/state/ReservationStateRegistry.java` | Maps status enum to state objects |
| `domain/model/Reservation.java` | Delegates `confirm()` / `cancel()` to current state |

### Factory Pattern — Reservation creation

Centralises construction of new reservations with consistent defaults.

| Location | Role |
|----------|------|
| `domain/factory/ReservationFactory.java` | Creates `PENDING` reservations with UUID, timestamp, and line items |

## 4. SOLID Principles

| Principle | Where |
|-----------|-------|
| **S — Single Responsibility** | `ReservationService` orchestrates use cases; `ReservationFactory` only creates reservations; state classes only handle transitions |
| **O — Open/Closed** | New reservation states can be added by implementing `ReservationState` without modifying existing state classes |
| **L — Liskov Substitution** | All `ReservationState` implementations are interchangeable via the registry |
| **I — Interface Segregation** | `ReservationRepositoryPort` and `InventoryRepositoryPort` expose only what the application layer needs |
| **D — Dependency Inversion** | `ReservationService` depends on port interfaces; JPA adapters in infrastructure implement them |

## 5. Database Design

### Tables

| Table | Purpose |
|-------|---------|
| `products` | Master product catalogue (SKU, name, description) |
| `inventory` | Stock levels per SKU: total, available, reserved |
| `reservations` | Reservation header: UUID id, order ID, status, created_at |
| `reservation_items` | Line items: reservation_id, SKU, quantity |

### Key decisions

- **UUID primary keys** for reservations — safe for distributed ID generation.
- **Separate inventory table** — stock is updated independently of product metadata.
- **`available_stock` + `reserved_stock`** — explicit tracking; `total_stock` is the ceiling.
- **`version` column** on inventory — optimistic locking as a secondary safety net alongside pessimistic locks.
- **CHECK constraints** — prevent negative stock at the database level.
- **Indexes** on `order_id` and `reservation_id` for lookup performance.
- **Liquibase SQL changesets** — all schema changes in versioned `.sql` files under `src/main/resources/db/changelog/changes/`.

## 6. Concurrency Safety

Concurrent reservations are handled with:

1. **`@Transactional`** — entire reserve operation is atomic.
2. **`SELECT … FOR UPDATE`** (pessimistic write lock) on inventory rows via `@Lock(LockModeType.PESSIMISTIC_WRITE)`.
3. **SKU ordering** — locks are acquired in sorted SKU order to prevent deadlocks on multi-item reservations.
4. **All-or-nothing validation** — if any SKU lacks stock, the entire reservation is rejected and the transaction rolls back.

## 7. Running the System

**Prerequisites:** Docker and Docker Compose.

```bash
docker compose up --build
```

This starts PostgreSQL, runs Liquibase migrations, seeds sample data, and launches the Spring Boot app on **http://localhost:8080**.

### Example API calls

**Reserve inventory:**
```bash
curl -X POST http://localhost:8080/api/v1/reservations \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-1001","items":[{"sku":"A100","quantity":5},{"sku":"B200","quantity":3}]}'
```

**Get reservation:**
```bash
curl http://localhost:8080/api/v1/reservations/{id}
```

**Confirm reservation:**
```bash
curl -X POST http://localhost:8080/api/v1/reservations/{id}/confirm
```

**Cancel reservation:**
```bash
curl -X POST http://localhost:8080/api/v1/reservations/{id}/cancel
```

**Get stock:**
```bash
curl http://localhost:8080/api/v1/inventory/A100
```

### Error responses

| HTTP Status | Scenario |
|-------------|----------|
| 409 Conflict | Insufficient stock, invalid state transition |
| 404 Not Found | Reservation or SKU not found |
| 400 Bad Request | Validation failure |

## 8. Running Tests

**Prerequisites:** Java 17, Maven, Docker (for Testcontainers integration test).

```bash
# All tests
mvn test

# Unit tests only
mvn test -Dtest="*Test"

# Integration test only
mvn test -Dtest="ConcurrentReservationIT"
```

### Test coverage

**Unit tests** (mocked repositories, no Spring context):
- Insufficient stock rejection
- Valid state transitions (PENDING → CONFIRMED, PENDING → CANCELLED)
- Invalid transitions (confirm/cancel on CONFIRMED or CANCELLED)
- Reservation factory creation
- Duplicate SKU aggregation in requests

**Integration test** (Testcontainers + real PostgreSQL):
- Two concurrent requests for the same SKU where combined quantity exceeds stock — exactly one succeeds, one is rejected

## 9. Trade-offs and Future Improvements

| Trade-off | Rationale | Improvement |
|-----------|-----------|-------------|
| Domain/JPA mapping boilerplate | Keeps domain free of JPA annotations | MapStruct or jMolecules for less manual mapping |
| Pessimistic locking | Simpler correctness under contention | Event sourcing or Redis-based distributed locks at very high scale |
| Eager fetch on reservation items | Small item lists per reservation | Lazy fetch + DTO projection for large orders |
| No idempotency key | Out of scope | Accept `Idempotency-Key` header on POST to safely retry |
| No reservation expiry | Out of scope | Scheduled job to auto-cancel stale PENDING reservations |

## 10. Scaling Considerations

| Bottleneck | What breaks | Fix |
|------------|-------------|-----|
| Row-level lock contention | High concurrent demand on hot SKUs causes queueing | Partition inventory by warehouse; cache read-only stock levels |
| Single PostgreSQL instance | Write throughput ceiling | Read replicas for GET endpoints; write master for reservations |
| Synchronous API | Latency under load | Async reservation queue with outbox pattern |
| No caching | Repeated inventory lookups hit DB | Redis cache for read-only stock queries with TTL |
| Monolithic deployment | Cannot scale reservation vs query independently | Split into reservation-service and inventory-query-service |

## Tech Stack

- Java 17
- Spring Boot 3.2
- PostgreSQL 16 (Docker)
- Spring Data JPA
- Liquibase (SQL changesets only)
- Maven
- JUnit 5 + Mockito + Testcontainers
>>>>>>> f42712c (Initial Commit Warehouse Inventory Reservation)
