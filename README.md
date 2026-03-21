# Car Sharing Platform — Distributed Microservices Backend System

A distributed backend platform for a car sharing service that allows users to list vehicles, search available cars, and manage bookings.

The system is built using a **microservices architecture with Spring Boot and Spring Cloud**, integrating service discovery, distributed tracing, centralized logging, monitoring, caching, and asynchronous messaging.
The entire environment can be started using **Docker Compose**, simulating a production-style backend infrastructure.

---

## System Architecture

The platform is composed of multiple microservices communicating via REST APIs and asynchronous messaging.

### Core Services

* **API Gateway** – single entry point for client requests and routing
* **User Service** – user management and authentication
* **Car Service** – car catalog management and search
* **Booking Service** – reservation and booking logic
* **Dispute Service** – dispute resolution between renters and owners

### Service Discovery

* **Eureka Server** – service registry and discovery

### Messaging

* **Kafka** – asynchronous communication between services. In this project it is also used for lifecycle commands: `booking-service` publishes `booking.commands` and `car-service` consumes it to update the car state.

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

* Car catalog and vehicle listing
* Booking and reservation management
* Dynamic pricing for bookings based on time and pickup location rules
* Promo codes and discounts applied on top of dynamic pricing
* Loyalty points earning and redemption linked to payments
* User authentication and authorization
* Dispute handling
* Distributed service discovery
* Centralized logging (ELK stack)
* Distributed tracing (Zipkin)
* Metrics monitoring (Prometheus + Grafana)
* Redis caching (catalog in `car-service`; optional guard, idempotency, read-cache in `booking-service`)
* Asynchronous messaging with Kafka
* Containerized microservices deployment

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

### Start the infrastructure and services

```bash
docker-compose up --build
```

---

## Business Flows

### Booking lifecycle

1. **Create booking** — Client sends `POST /api/v1/bookings` with renter, car, time range and pickup location. Booking Service validates that the user exists (User Service), the car is available (Car Service), and **no other active booking overlaps the same car and time**; then creates a booking in status `PENDING`.
2. **Approve** — `PATCH /api/v1/bookings/{id}` with `{"status": "APPROVED"}` moves the booking from `PENDING` to `APPROVED`.
3. **Complete** — Same endpoint with `{"status": "COMPLETED"}` moves from `APPROVED` to `COMPLETED`. **A completed payment is required** for the booking before it can be completed.
4. **Cancel** — `{"status": "CANCELED"}` is allowed from `PENDING` or `APPROVED`; completed bookings cannot be canceled.

After every lifecycle status change in `booking-service` (`APPROVED`, `COMPLETED`, `CANCELED`), the service publishes a command to Kafka topic `booking.commands`. The `car-service` consumes this command and updates the car state accordingly by calling `rentCar` / `returnCar`.

### Payment flow

1. **Create payment** — For an existing booking, `POST /api/v1/bookings/{bookingId}/payment` creates a payment (method, status `PENDING`). The **amount is calculated inside Booking Service** using:
   * base price derived from booking duration,
   * dynamic pricing rules (time-of-day and pickup location multipliers),
   * optional promo code discounts,
   * optional loyalty points redemption.
2. **Confirm** — `PATCH /api/v1/admin/payments/{paymentId}/confirm` sets payment to `COMPLETED`. At this moment, the renter **earns loyalty points** based on the paid amount. The action is **audited** in `admin_audit_log` (payment id, action `PAYMENT_CONFIRM`, optional actor from header `X-User-Id`).
3. **Refund** — `PATCH /api/v1/admin/payments/{paymentId}/refund` is allowed only for `COMPLETED` payments. The action is **audited** in `admin_audit_log` (action `PAYMENT_REFUND`, optional `X-User-Id`). (Loyalty adjustments for refunds can be extended in future iterations.)

### Security and roles via API Gateway

* All secured requests pass through the API Gateway, where JWT tokens are validated.
* After successful validation, the gateway forwards user identity and roles to downstream services via headers:
  * `X-User-Id` — numeric user identifier from the `userId` claim.
  * `X-User-Roles` — comma-separated list of roles from the `authorities` claim (for example `ROLE_USER,ROLE_ADMIN`).
* Endpoints under `/api/v1/admin/**` are additionally protected at the gateway level and require the `ROLE_ADMIN` authority. Requests without this role receive `403 FORBIDDEN` before reaching downstream services.

---

## Tests and coverage

All tests are integration-style: they use the Spring context, in-memory H2 where applicable, and mock external calls where needed.

**Run all tests** (from project root; builds all modules and runs tests):

```bash
mvn test
```

**Run tests for a single module** (build dependencies first, or use `-am`):

```bash
mvn test -pl booking-service -am
mvn test -pl api-gateway -am
```

If you already ran `mvn install` from the root, you can use `mvn test -pl booking-service` without `-am`.

**Run a specific test class:**

```bash
mvn test -pl booking-service -Dtest=BookingControllerIntegrationTest
```

**Generate JaCoCo coverage report** (for modules that define the JaCoCo plugin):

```bash
mvn clean test jacoco:report
```

Reports are written under `target/site/jacoco/index.html` in each module.

---

## Service Endpoints

API Gateway
http://localhost:8085

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

* **api-gateway** — entry point, routing, JWT validation
* **user-service** — users and auth
* **car-service** — car catalog and availability
* **booking-service** — bookings, payments, transactions
* **dispute-service** — disputes
* **common-utils** — JWT and shared utilities
* **api-contracts** — shared DTOs and enums
* **eureka-server** — service discovery

* **infrastructure** — Docker and config for Prometheus, Grafana, Logstash, scripts  
* **docker-compose.yml** — full stack run

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
* Kafka-based asynchronous processing
* Database indexing strategies
* Connection pooling
* JVM profiling and GC tuning scripts

### Booking service Redis (optional)

`booking-service` can use Redis for concurrency, idempotency, and read caching. Each feature is **disabled by default** in `booking-service/src/main/resources/application.yml`; enable the flags you need and point `spring.data.redis.host` / `spring.data.redis.port` at your Redis instance.

| Feature | Property | Notes |
|--------|----------|--------|
| Booking creation lock | `application.redis.booking-guard.enabled` | Per-car distributed lock while creating a booking (reduces race conditions). |
| Idempotency | `application.redis.idempotency.enabled` | For duplicate-safe retries, send optional header `Idempotency-Key` on `POST /api/v1/bookings` and `POST /api/v1/bookings/{bookingId}/payment`. |
| Read cache | `application.redis.read-cache.enabled` | Spring Cache on Redis for read-heavy booking queries; eviction on mutations and after booking lifecycle events (after commit). |

Shared naming prefix: `application.redis.key-prefix` (default `booking`). Micrometer metrics are exposed for the guard and idempotency components (e.g. `booking.redis.guard.*`, `booking.redis.idempotency.*`).

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

## Author
Dumitru Diacenco, Java Backend Engineer
