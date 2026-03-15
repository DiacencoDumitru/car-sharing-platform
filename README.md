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

* **Kafka** – asynchronous communication between services

### Data Layer

* **PostgreSQL** – primary relational database
* **Redis** – caching layer

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
* User authentication and authorization
* Dispute handling
* Distributed service discovery
* Centralized logging (ELK stack)
* Distributed tracing (Zipkin)
* Metrics monitoring (Prometheus + Grafana)
* Redis caching
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

git clone https://github.com/DiacencoDumitru/car-sharing-platform.git

cd car-sharing-platform

---

### Build the project

mvn clean package

---

### Start the infrastructure and services

docker-compose up --build

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

api-gateway
user-service
car-service
booking-service
dispute-service
common-utils
eureka-server

infrastructure
prometheus
grafana
logstash
scripts

docker-compose.yml

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

* Redis caching for frequently accessed data
* Kafka-based asynchronous processing
* Database indexing strategies
* Connection pooling
* JVM profiling and GC tuning scripts

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

- Dumitru Diacenco
- Java Backend Engineer
