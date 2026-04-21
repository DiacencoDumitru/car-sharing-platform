# Car Sharing Platform — Distributed Microservices Backend System

A distributed backend platform for a car sharing service that allows users to list vehicles, search available cars, and manage bookings.

The system is built using a **microservices architecture with Spring Boot and Spring Cloud**, integrating service discovery, distributed tracing, centralized logging, monitoring, caching, and asynchronous messaging.
The entire environment can be started using **Docker Compose**, simulating a production-style backend infrastructure.

---

## System Architecture

The platform is composed of multiple microservices communicating via REST APIs and asynchronous messaging.

### Core Services

* **API Gateway** – single entry point for client requests and routing
* **User Service** – user management and authentication; renter **favorite cars** at `/api/v1/users/me/favorite-cars` and `/api/v1/users/{userId}/favorite-cars` (table `favorite_cars`; **car-service** validates on add); **my bookings** at `GET /api/v1/users/me/bookings` (optional `asRole=renter|owner`, JWT) proxied to **booking-service** internal APIs; **my transactions** at `GET /api/v1/users/me/transactions` (paginated union of renter and owner-side activity via **car-service** car ids)
* **Car Service** – car catalog management and search; optional `ownerId` on `GET /api/v1/cars` to list a user’s listed vehicles (used when resolving owner-scoped bookings and transactions); optional `minAverageRating` on the same endpoint to return only cars with a non-null average review rating at or above the given value; optional `minReviewCount` to require at least that many stored car reviews (missing counts treated as zero)
* **Booking Service** – reservation and booking logic; **availability check** at `GET /api/v1/bookings/availability?carId=&startTime=&endTime=` (ISO-8601 query parameters) returns whether the car is listed as available and has no overlapping PENDING or APPROVED booking; **daily availability calendar** at `GET /api/v1/bookings/availability/calendar?carId=&startTime=&endTime=` returns per-day availability for the selected interval
* **Notification Service** – consumes booking lifecycle events from Kafka, stores analytics and fraud signals, and can dispatch email/push notifications when a case needs attention (integrates with **User Service** for contact data); also consumes **`booking.reminders`** for scheduled rental start/end reminders published by **booking-service**
* **Dispute Service** – dispute resolution between renters and owners

### Service Discovery

* **Eureka Server** – service registry and discovery

### Messaging

* **Kafka** – asynchronous communication between services. **Booking lifecycle** events are published to topic `booking.commands` (via transactional outbox in `booking-service`). **Consumers:** `car-service` updates vehicle availability; **`notification-service`** records analytics, runs a lightweight **anti-fraud** heuristic, and optionally sends notifications. Each consumer uses its **own consumer group**, so all subscribers receive the same event stream.

* **Transactional outbox (booking-service)** – when Kafka is enabled, lifecycle events are **written to table `booking_lifecycle_outbox` in the same database transaction** as the booking status change, then a **relay** publishes to Kafka and removes the row. This avoids losing events if the process crashes after commit but before a broker send (see [Transactional outbox](#transactional-outbox-booking-service)).

### Data Layer

* **PostgreSQL** – primary relational database
* **Redis** – caching layer (`car-service`); optional distributed guard, idempotency, and read-cache in `booking-service` (see [Booking service Redis](#booking-service-redis-optional))

### Observability Stack

* **Prometheus** – metrics collection
* **Grafana** – metrics visualization dashboards
* **Elasticsearch + Logstash + Kibana (ELK)** – centralized logging
* **Zipkin** – distributed tracing across microservices

### Containerization

* **Docker**
* **Docker Compose**

---

## Architecture Overview

Client
│
API Gateway
│
├── User Service (2 instances)
├── Car Service
├── Booking Service
├── Notification Service
└── Dispute Service

Infrastructure
├── Eureka Server
├── PostgreSQL
├── Redis
├── Kafka
├── Prometheus
├── Grafana
├── Elasticsearch
├── Logstash
├── Kibana
└── Zipkin

---

## Tech Stack

### Backend

* Java 17
* Spring Boot
* Spring Cloud
* Spring Security
* Spring Data JPA
* Maven

### Data

* PostgreSQL
* H2 (testing)
* Redis

### Messaging

* Apache Kafka

### Observability

* Prometheus
* Grafana
* Elasticsearch
* Logstash
* Kibana
* Zipkin

### Infrastructure

* Docker
* Docker Compose

---

## Key Features

* Car catalog and vehicle listing; browse filters on `GET /api/v1/cars` include optional **`minAverageRating`** (cars without an average rating are omitted when set) and optional **`minReviewCount`** (minimum number of car reviews, treating a missing count as zero)
* Booking and reservation management
* Daily availability calendar for a selected car and interval
* Dynamic pricing for bookings based on time and pickup location rules
* Price quote before booking with detailed cost breakdown (base, dynamic markup, discounts, loyalty)
* Promo codes and discounts applied on top of dynamic pricing
* Loyalty points earning and redemption linked to payments
* **Favorite cars (wishlist)** — `GET` / `PUT` / `DELETE` on `/api/v1/users/me/favorite-cars` (JWT) and the same on `/api/v1/users/{userId}/favorite-cars` when the token matches that user; **car-service** is used when adding a favorite
* **Favorite car availability alerts** — when a booking lifecycle event marks a car as `COMPLETED` or `CANCELED`, **notification-service** can notify users who have that car in favorites via email/push (resolved through **user-service** internal endpoint)
* **Rental reminders** — with Kafka enabled, **booking-service** runs a scheduler that publishes **`BookingReminderEventDto`** messages to topic **`booking.reminders`** when an approved booking enters the configured window before `startTime` or `endTime` (deduplicated in `booking_reminder_sent`). **notification-service** consumes the topic and dispatches email/push (idempotent dispatch log). Configure `application.reminders.*` and `application.messaging.topics.booking-reminders` in **booking-service** / **notification-service**.
* **Car reviews with star rating** — creating a review requires `bookingId`, `rating` (1–5), and a completed booking validated via **booking-service** internal `GET /api/v1/internal/bookings/{bookingId}/for-review`; **car-service** exposes `averageRating` and `reviewCount` on **`CarDto`** (also reflected in Elasticsearch car documents).
* **Booking summary (aggregate)** — `GET /api/v1/bookings/{bookingId}/summary` returns the **booking**, optional **payment** for that booking, and **transactions** linked to the same booking in one JSON payload (`204` when the booking does not exist); useful for detail screens without chaining multiple calls
* **My bookings and transactions (user-service)** — `GET /api/v1/users/me/bookings?asRole=renter|owner` and `GET /api/v1/users/me/transactions` (JWT); **booking-service** exposes internal routes `/api/v1/internal/users/{userId}/bookings` and `/api/v1/internal/users/{userId}/transactions` for service-to-service use after JWT validation in **user-service**
* Admin filtering for payments and transactions via criteria-based search endpoints
* User authentication and authorization
* Dispute handling
* Distributed service discovery
* Centralized logging (ELK stack)
* Distributed tracing (Zipkin)
* Metrics monitoring (Prometheus + Grafana)
* Redis caching (catalog in `car-service`; optional guard, idempotency, read-cache in `booking-service`)
* Redis-backed response caching in `api-gateway` for read-heavy GET endpoints with TTL and mutation-driven invalidation
* Asynchronous messaging with Kafka
* **Transactional outbox** for reliable booking lifecycle events to Kafka (`booking-service`)
* **Notification pipeline** for booking lifecycle: idempotent analytics storage, fraud scoring, conditional email/push (`notification-service`)
* Containerized microservices deployment

---

## Transactional outbox (booking-service)

When `application.messaging.kafka.enabled=true`, booking lifecycle events (`APPROVED`, `COMPLETED`, `CANCELED` when the car must be returned) are **stored in PostgreSQL** in table `booking_lifecycle_outbox` **in the same transaction** as the booking update. A **scheduled relay** polls pending rows, publishes to Kafka topic `booking.commands` (via `BookingLifecycleKafkaEventPublisher`), and **deletes** the row after a successful send. If Kafka is temporarily down, rows remain and are retried.

**Related settings** in `services/booking-service/src/main/resources/application.yml`:

| Property | Purpose |
|----------|---------|
| `application.messaging.kafka.enabled` | Turn Kafka + outbox + relay on (`false` by default in the file; Docker Compose sets it to `true` for the booking container). |
| `application.messaging.outbox.relay-interval-ms` | How often the relay runs (default `1000`). |
| `application.messaging.outbox.batch-size` | Max rows processed per scheduler tick (default `50`). |
| `application.messaging.outbox.send-timeout-seconds` | Sync Kafka send timeout used by the publisher (default `10`). |

Cache eviction for Redis read-cache still uses **after-commit** application events; only the **Kafka** path goes through the outbox.

---

## Notification service

`notification-service` listens to **`booking.commands`** when `application.messaging.kafka.enabled=true` (enabled in the **`local-docker`** profile; default in `application.yml` is `false` for local runs without Kafka).

**Processing pipeline**

1. **Idempotency** – events are deduplicated using table `booking_lifecycle_analytics_events` with a unique constraint on `(booking_id, booking_status)` (at-least-once Kafka delivery).
2. **Anti-fraud** – `SimpleAntiFraudService` assigns a risk score and `attentionRequired`; rules include heuristics on renter id and time between `APPROVED` and later `COMPLETED` / `CANCELED` (configurable window `fraud.approve-cancel-window-seconds`).
3. **Fraud notifications** – if `attentionRequired` is true, the service may send **email** and/or **push** via HTTP (`notifications.email.http.endpoint-url`, `notifications.push.http.endpoint-url`), resolving renter **email** and **phone** through **User Service** (`lb://user-service`, Eureka load-balanced WebClient).
4. **Favorite availability notifications** – for lifecycle statuses `COMPLETED` and `CANCELED`, the service asks **User Service** for users who favorited the car (`GET /api/v1/internal/cars/{carId}/favorite-users`) and dispatches email/push for each recipient.
5. **Dead-letter topic** – repeated processing failures are sent to `booking.commands.dlt` (see `KafkaConfig`).

**Related settings** in `services/notification-service/src/main/resources/application.yml` (and profile overrides):

| Property | Purpose |
|----------|---------|
| `application.messaging.kafka.enabled` | Enable Kafka listener and `KafkaConfig` beans. |
| `application.messaging.topics.booking-commands` | Topic name (default `booking.commands`). |
| `application.messaging.topics.booking-commands-dlt` | DLT topic for failed records. |
| `fraud.approve-cancel-window-seconds` | Window for “fast” approve→complete/cancel heuristics (default `3600`). |
| `notifications.dispatch.email-enabled` / `push-enabled` | Toggle channels. |

---

## Running the Project

### Prerequisites

* Java 17
* Maven
* Docker
* Docker Compose

---

### Clone the repository

```bash
git clone https://github.com/DiacencoDumitru/car-sharing-platform.git
cd car-sharing-platform
```

### Build the project

```bash
mvn clean package
```

### JVM / GC profiling (optional)

Scripts under `scripts/jvm/` run a **locally built** Spring Boot JAR with G1 GC and GC logging to files such as `gc-baseline-*.log` in the current working directory. They are templates for tuning and profiling, not part of the default Docker workflow.

By default they look for `services/booking-service/target/booking-service-*-SNAPSHOT.jar` after `mvn package` (or `mvn -pl services/booking-service -am package`). To run another service, set **`JAR_PATH`** to the JAR file or pass its path as the first argument, for example:

```bash
./scripts/jvm/run-server-gc-default.sh
JAR_PATH=services/car-service/target/car-service-1.0-SNAPSHOT.jar ./scripts/jvm/run-server-gc-default.sh
./scripts/jvm/run-server-gc-fewer-pauses.sh services/user-service/target/user-service-1.0-SNAPSHOT.jar
```

### Start the infrastructure and services (recommended)

This is the **simplest way** to run the whole platform: all microservices, databases, Kafka, Redis, Eureka, observability, etc.

Create a local env file from the template and set secrets (database password, `JWT_SECRET`, Grafana admin password):

```bash
cp .env.example .env
```

Edit `.env` and fill required values (empty passwords will not work for PostgreSQL). Then start the stack:

```bash
docker compose up --build
```

Wait until health checks pass; then use the [Service Endpoints](#service-endpoints) (e.g. API Gateway on port **8085**).

### Run microservices separately (optional, for local development)

You **do not** have to start every service manually if you use Docker Compose above. If you prefer to run one or more Spring Boot apps from the IDE or `mvn spring-boot:run`:

1. **Start shared infrastructure** (at minimum PostgreSQL, Redis, Kafka, Eureka). For example, only infra containers:

   ```bash
   docker compose up -d db redis kafka eureka-server
   ```

   Add other containers (Zipkin, Logstash, …) if your modules expect them.

2. **Point services at `localhost`** – default `application.yml` files use Docker hostnames (e.g. `dynamiccarsharing_db`, `kafka`, `eureka-server`). When running a service on the host, override with environment variables or a local profile, for example:

   * `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/dynamic_car_sharing_db`
   * `SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092`
   * `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/`

3. **Startup order** – start **Eureka** first, then domain services (**user**, **car**, **booking**, **notification**, **dispute**), then **API Gateway**. For booking lifecycle events to reach Kafka consumers, enable **`APPLICATION_MESSAGING_KAFKA_ENABLED=true`** on `booking-service` and **`APPLICATION_MESSAGING_KAFKA_ENABLED=true`** on `notification-service` (or use the **`local-docker`** profile for the notification module, which enables it in `application-local-docker.yml`).

---

## Business Flows

### Booking lifecycle

1. **Create booking** — Client sends `POST /api/v1/bookings` with renter, car, time range and pickup location. Booking Service validates that the user exists (User Service), the car is available (Car Service), and **no other active booking overlaps the same car and time**; then creates a booking in status `PENDING`.
2. **Approve** — `PATCH /api/v1/bookings/{id}` with `{"status": "APPROVED"}` moves the booking from `PENDING` to `APPROVED`.
3. **Complete** — Same endpoint with `{"status": "COMPLETED"}` moves from `APPROVED` to `COMPLETED`. **A completed payment is required** for the booking before it can be completed.
4. **Cancel** — `{"status": "CANCELED"}` is allowed from `PENDING` or `APPROVED`; completed bookings cannot be canceled.

After every lifecycle status change in `booking-service` (`APPROVED`, `COMPLETED`, `CANCELED` when applicable), the service **records an outbox row** (with Kafka enabled) and the **outbox relay** publishes to Kafka topic `booking.commands`. The `car-service` consumes this command and updates the car state accordingly by calling `rentCar` / `returnCar`. **`notification-service`** consumes the same topic for analytics, fraud scoring, and optional notifications. Consumers should treat messages as **at-least-once** and stay **idempotent** where needed.

### Favorite availability notifications

1. User adds cars to favorites through `/api/v1/users/me/favorite-cars` or `/api/v1/users/{userId}/favorite-cars`.
2. Booking lifecycle event with status `COMPLETED` or `CANCELED` is published to `booking.commands`.
3. `notification-service` resolves favorite subscribers by car id through `GET /api/v1/internal/cars/{carId}/favorite-users`.
4. `notification-service` sends email and push notifications to resolved recipients when channels are enabled.

### Payment flow

1. **Create payment** — For an existing booking, `POST /api/v1/bookings/{bookingId}/payment` creates a payment (method, status `PENDING`). The **amount is calculated inside Booking Service** using:
   * base price derived from booking duration,
   * dynamic pricing rules (time-of-day and pickup location multipliers),
   * optional promo code discounts,
   * optional loyalty points redemption.
2. **Confirm** — `PATCH /api/v1/admin/payments/{paymentId}/confirm` sets payment to `COMPLETED`. At this moment, the renter **earns loyalty points** based on the paid amount. The action is **audited** in `admin_audit_log` (payment id, action `PAYMENT_CONFIRM`, optional actor from header `X-User-Id`).
3. **Refund** — `PATCH /api/v1/admin/payments/{paymentId}/refund` is allowed only for `COMPLETED` payments. **Loyalty is reversed** for that payment: points earned on confirmation are deducted, and points redeemed when the payment was created are credited back to the renter’s loyalty account. If the account balance is insufficient to deduct earned points (for example, the renter spent points elsewhere after the payment), the refund fails with a validation error. The action is **audited** in `admin_audit_log` (action `PAYMENT_REFUND`, optional `X-User-Id`).

### Quote flow

1. **Request quote** — `POST /api/v1/bookings/quote` returns a pre-booking estimate for a renter and car in a selected time window.
   * Optional retry-safety header: `Idempotency-Key`. Repeated requests with the same key return the same quote response.
2. **Availability check** — Booking Service verifies that the car is currently available through Car Service before pricing.
3. **Pricing breakdown** — Quote includes:
   * base amount,
   * dynamic markup amount,
   * promo discount amount,
   * loyalty preview amount,
   * final total amount.
4. **Use quote for booking/payment** — The response is intended for UI transparency before creating the booking and payment.

Example request:

```json
{
  "renterId": 10,
  "carId": 303,
  "startTime": "2026-04-10T10:00:00",
  "endTime": "2026-04-10T12:00:00",
  "pickupLocationId": 1,
  "promoCode": "PROMO20",
  "loyaltyPointsToUse": 3
}
```

Example response:

```json
{
  "renterId": 10,
  "carId": 303,
  "baseAmount": 20.00,
  "dynamicMarkupAmount": 4.00,
  "discountAmount": 4.80,
  "loyaltyAmount": 3.00,
  "totalAmount": 16.20,
  "currency": "USD",
  "expiresAt": "2026-04-10T09:15:00"
}
```

### Admin search endpoints (criteria-based)

Booking Service exposes criteria-based filtering for admin read APIs without introducing separate `/search` routes.

* `GET /api/v1/admin/payments`
  * Optional query params: `bookingId`, `amount`, `status`, `paymentMethod`
* `GET /api/v1/admin/transactions`
  * Optional query params: `bookingId`, `status`, `paymentMethod`

Controller layer detects whether any filter is provided:
* with filters -> delegates to `search*` service methods
* without filters -> returns full list methods

This keeps controllers thin and preserves a consistent API style for admin read endpoints.

### Favorite cars (user-service)

Persisted in `favorite_cars`. Every add checks **car-service** for a real car.

1. **`/api/v1/users/me/favorite-cars`** — `GET` returns a JSON array of car IDs (most recently added first). `PUT` / `DELETE` `/{carId}` are idempotent (`204`).
2. **`/api/v1/users/{userId}/favorite-cars`** — same when the JWT subject matches `userId`; `GET` returns `{ "carIds": [ … ] }` sorted by car ID.
3. **`/api/v1/internal/cars/{carId}/favorite-users`** — internal service endpoint used by `notification-service`; returns user IDs sorted ascending.

### Security and roles via API Gateway

* All secured requests pass through the API Gateway, where JWT tokens are validated.
* After successful validation, the gateway forwards user identity and roles to downstream services via headers:
  * `X-User-Id` — numeric user identifier from the `userId` claim.
  * `X-User-Roles` — comma-separated list of roles from the `authorities` claim (for example `ROLE_USER,ROLE_ADMIN`).
* Endpoints under `/api/v1/admin/**` are additionally protected at the gateway level and require the `ROLE_ADMIN` authority. Requests without this role receive `403 FORBIDDEN` before reaching downstream services.

---

## Tests and coverage

Most tests are **integration-style**: they load the Spring context, use in-memory H2 or Testcontainers where applicable, and mock external HTTP calls where needed.

**Run all tests** (from the **repository root**; runs every module in the reactor):

```bash
mvn test
```

**Run tests for a single module** (build dependent modules first, or use `-am`):

```bash
mvn test -pl booking-service -am
mvn test -pl api-gateway -am
```

If you already ran `mvn install` from the root, you can usually run:

```bash
mvn test -pl booking-service
```

**Run one test class** (example: booking REST; example: transactional outbox + embedded Kafka):

```bash
mvn test -pl booking-service -Dtest=BookingControllerIntegrationTest
mvn test -pl booking-service -Dtest=BookingLifecycleOutboxIntegrationTest
mvn test -pl notification-service -am -Dtest=BookingLifecycleCommandListenerIntegrationTest
```

Some tests use **Testcontainers** (e.g. Redis) and are skipped or require Docker; see `@Testcontainers(disabledWithoutDocker = true)` in those classes.

**Generate JaCoCo coverage report** (for modules that define the JaCoCo plugin):

```bash
mvn clean test jacoco:report
```

Reports are written under `target/site/jacoco/index.html` in each module.

### Quick checklist to “test the application”

| Goal | Command / action |
|------|------------------|
| Verify all modules compile and tests pass | `mvn test` from the **root** |
| Focus on **booking-service** | `mvn test -pl booking-service -am` |
| Run the **full stack** and test via HTTP | `docker-compose up --build`, then call the API through the gateway (see [Service Endpoints](#service-endpoints)) |
| Health | Open Eureka (`8761`) or each service’s `/actuator/health` |

---

## Service Endpoints

API Gateway
http://localhost:8085

Notification Service (actuator)
http://localhost:8084

Eureka Dashboard
http://localhost:8761

Prometheus
http://localhost:9090

Grafana
http://localhost:3000

Kibana
http://localhost:5601

Zipkin
http://localhost:9411

PostgreSQL
localhost:5432

Redis
localhost:6379

Kafka
localhost:9092

---

## Project Structure

* **`services/`** — runnable Spring Boot applications (microservices and edge)

  * **api-gateway** — entry point, routing, JWT validation
  * **user-service** — users and auth
  * **car-service** — car catalog and availability
  * **booking-service** — bookings, payments, transactions
  * **notification-service** — booking lifecycle Kafka consumer, analytics, fraud heuristics, notification dispatch
  * **dispute-service** — disputes
  * **eureka-server** — service discovery

* **`shared/`** — libraries consumed by services

  * **api-contracts** — shared DTOs and enums
  * **common-utils** — JWT and shared utilities

* **`infrastructure/`** — Prometheus, Grafana, and Logstash configs used by Docker Compose  
* **`infra/`** — Terraform (GitLab validate/plan)  
* **`docker-compose.yml`** — full stack run

---

## Observability

The system includes a full observability stack:

* **Prometheus** collects metrics from microservices
* **Grafana** visualizes system performance dashboards
* **Logstash** collects and processes application logs
* **Elasticsearch** stores and indexes logs
* **Kibana** provides log visualization
* **Zipkin** enables distributed tracing across services

This setup allows monitoring request flows, system health, and service performance.

---

## Performance Considerations

The platform includes several performance optimizations:

* Redis caching for frequently accessed data (`car-service` catalog; optional read-cache in `booking-service` — see below)
* Redis response cache in `api-gateway` for selected GET endpoints with identity-aware keys
* Kafka-based asynchronous processing
* Database indexing strategies
* Connection pooling
* JVM profiling and GC tuning scripts

### Booking service Redis (optional)

`booking-service` can use Redis for concurrency, idempotency, and read caching. Each feature is **disabled by default** in `services/booking-service/src/main/resources/application.yml`; enable the flags you need and point `spring.data.redis.host` / `spring.data.redis.port` at your Redis instance.

| Feature | Property | Notes |
|--------|----------|--------|
| Booking creation lock | `application.redis.booking-guard.enabled` | Per-car distributed lock while creating a booking (reduces race conditions). |
| Idempotency | `application.redis.idempotency.enabled` | For duplicate-safe retries, send optional header `Idempotency-Key` on `POST /api/v1/bookings` and `POST /api/v1/bookings/{bookingId}/payment`. |
| Read cache | `application.redis.read-cache.enabled` | Spring Cache on Redis for read-heavy booking queries; eviction on mutations and after booking lifecycle events (after commit). |

Shared naming prefix: `application.redis.key-prefix` (default `booking`). Micrometer metrics are exposed for the guard and idempotency components (e.g. `booking.redis.guard.*`, `booking.redis.idempotency.*`).

### API Gateway Redis response cache

`api-gateway` caches selected `GET` responses in Redis (`application.cache.response.enabled=true`) and reuses them until TTL expires.

Default cache settings in `services/api-gateway/src/main/resources/application.yml`:

| Property | Purpose |
|----------|---------|
| `application.cache.response.default-ttl` | TTL for cached gateway responses. |
| `application.cache.response.max-body-size-bytes` | Upper size bound for a cacheable response body. |
| `application.cache.response.cacheable-paths` | GET endpoint patterns that can be cached. |
| `application.cache.response.invalidation` | Path-group map used to invalidate cached GET data after successful write operations. |

Behavior:

* only `GET` and only `200 OK` responses are cached
* responses with `Cache-Control: no-store` or `private` are not cached
* secured endpoints are isolated by `Authorization` header hash in the cache key
* successful `POST`/`PUT`/`PATCH`/`DELETE` requests trigger group-based invalidation for related cached GET keys

Run integration tests for this feature:

```bash
mvn test -pl api-gateway -Dtest=ResponseCacheIntegrationTest
```

---

## Security

Authentication and authorization are implemented using:

* Spring Security
* JWT-based authentication
* Role-based access control

---

## Learning Goals

This project demonstrates practical experience with:

* microservices architecture
* distributed systems design
* service discovery
* API gateway patterns
* distributed tracing
* centralized logging
* containerized infrastructure
* backend scalability

---

## Architecture and Design Patterns

This codebase applies practical microservice design patterns focused on reliability and maintainability.

### Typed Integration Clients (Ports and Adapters)

Inter-service validations are isolated behind typed integration ports instead of being embedded in domain services:

- `booking-service` uses integration clients for `user-service` and `car-service`
- `dispute-service` uses integration clients for `user-service` and `booking-service`

Why it helps:

- keeps business services focused on domain rules
- centralizes HTTP details, error mapping, retries, and timeout policy
- reduces accidental contract drift between services

### Resilience Strategy for Inter-service Calls

For outbound HTTP calls, integration clients apply:

- bounded timeout (`application.http.clients.timeout-seconds`)
- retry with backoff for retriable failures (`application.http.clients.retry-max-attempts`, `application.http.clients.retry-backoff-millis`)
- consistent exception mapping:
  - `404` -> validation-level business error
  - `5xx` / transport failures -> service-level technical error

### Interview Demo (5 minutes)

1. Run tests proving integration client behavior:

```bash
mvn test -pl booking-service -Dtest=IntegrationClientsIntegrationTest
mvn test -pl dispute-service -Dtest=IntegrationClientsIntegrationTest
```

2. Explain the before/after:
   - before: `WebClient` + URI building + try/catch inside business services
   - after: business services call typed integration ports, resilience is centralized

3. Show that failure paths are deterministic:
   - upstream `404` is mapped to business validation errors
   - upstream `5xx` is mapped to technical service errors

---

## Author
Dumitru Diacenco, Java Backend Engineer
