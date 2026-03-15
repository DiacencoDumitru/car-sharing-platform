# Car Sharing Platform — Microservices Backend System

A distributed backend platform for a car sharing service that allows users to list vehicles, search available cars, and manage bookings.

The system is implemented using a microservices architecture with Spring Boot and integrates messaging, caching, monitoring, and containerized deployment.

This project demonstrates production-style backend architecture including service discovery, API gateway routing, distributed logging, monitoring, and container orchestration.

---

## System Architecture

The platform is composed of multiple independent microservices communicating through REST APIs and messaging.

Core services:

* API Gateway – central entry point for all client requests
* User Service – user management and authentication
* Car Service – car catalog and vehicle listings
* Booking Service – booking management
* Dispute Service – dispute handling and resolution

Infrastructure services:

* Eureka Server – service discovery
* Kafka – asynchronous messaging
* PostgreSQL – relational database
* Redis – caching layer
* Prometheus + Grafana – monitoring and metrics
* Elasticsearch + Logstash – centralized logging
* Docker Compose – container orchestration

---

## Architecture Overview

Client
│
API Gateway
│
├── User Service
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
└── Elasticsearch / Logstash

---

## Tech Stack

Backend

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* Spring Cloud

Data

* PostgreSQL
* H2 (testing)
* Redis

Messaging

* Kafka

Observability

* Prometheus
* Grafana
* Elasticsearch
* Logstash

Infrastructure

* Docker
* Docker Compose
* Maven

---

## Key Features

* Car catalog and vehicle listings
* User management and authentication
* Car booking and reservation system
* Dispute management
* Rating and review system
* REST API communication between services
* Distributed service discovery
* Monitoring and metrics collection
* Centralized logging
* Containerized deployment

---

## Running the Project

### Prerequisites

* Java 17
* Maven
* Docker
* Docker Compose

### Clone the repository

git clone https://github.com/DiacencoDumitru/car-sharing-platform.git

### Build the project

mvn clean package

### Run with Docker

docker-compose up --build

---

## Access Services

API Gateway
http://localhost:8080

Monitoring
Grafana: http://localhost:3000
Prometheus: http://localhost:9090

Database management
pgAdmin: http://localhost:5050

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

---

## Observability

The platform includes monitoring and logging components:

* Prometheus collects application metrics
* Grafana visualizes performance dashboards
* Logstash + Elasticsearch aggregate and store logs
* JVM profiling tools included for performance analysis

This setup allows monitoring system health, request throughput, and service performance.

---

## Performance Considerations

The system includes several performance optimizations:

* Redis caching for frequently accessed data
* Database indexing strategies
* Connection pooling
* Asynchronous messaging via Kafka / RabbitMQ
* JVM profiling and GC tuning scripts

---

## Security

Authentication and authorization implemented using:

* Spring Security
* Secure REST API communication
* Role-based access control

---

## Learning Goals

This project demonstrates practical experience with:

* microservices architecture
* distributed systems
* REST API design
* backend scalability
* monitoring and observability
* containerized environments

---

## Author

Dumitru Diacenco
Java Backend Engineer

GitHub: https://github.com/DiacencoDumitru
LinkedIn: https://linkedin.com/in/dumitru-diacenco-198121283
