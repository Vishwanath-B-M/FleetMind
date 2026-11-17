# RideFlow – Event-Driven Ride-Hailing Backend

An event-driven, microservices-based ride-hailing backend built using **Java, Spring Boot, Apache Kafka, Redis, PostgreSQL, and Docker**.

RideFlow demonstrates how independent backend services communicate to process ride requests, locate nearby drivers, match drivers with riders, and manage ride lifecycles.

## Architecture

```text
                  Rider Application
                         |
                         | HTTP REST
                         v
                 +------------------+
                 |   Ride Service   |
                 |     :8083        |
                 |                  |
                 | Ride Management  |
                 | PostgreSQL       |
                 +--------+---------+
                          |
                          | Kafka: ride.requested
                          v
                 +------------------+
                 | Matching Service |
                 |     :8084        |
                 |                  |
                 | Driver Matching  |
                 +--------+---------+
                          |
                          | OpenFeign
                          v
                 +------------------+
                 | Location Service |
                 |     :8082        |
                 |                  |
                 | Redis GEO Search |
                 +--------+---------+
                          |
                          v
                     +--------+
                     | Redis  |
                     |  GEO   |
                     +--------+

       Matching Service
              |
              | Kafka: ride.matched
              v
         Ride Service
              |
              v
       Driver Assignment
```

## Microservices

| Service | Port | Responsibility | Technology |
|---|---:|---|---|
| Ride Service | 8083 | Ride requests, lifecycle, fare estimation and persistence | Spring Boot, PostgreSQL |
| Location Service | 8082 | Driver location updates and nearby-driver search | Spring Boot, Redis GEO |
| Matching Service | 8084 | Driver selection and event processing | Spring Boot, Kafka, OpenFeign |

## Tech Stack

**Backend**
- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Data Redis
- Spring Kafka
- Spring Cloud OpenFeign
- Spring Boot Actuator

**Databases and Messaging**
- PostgreSQL
- Redis GEO
- Apache Kafka
- Apache ZooKeeper

**DevOps and Tools**
- Docker
- Docker Compose
- Maven
- IntelliJ IDEA
- Postman
- Git and GitHub

## Key Features

### 1. Ride Management
- Create and persist ride requests in PostgreSQL.
- Estimate ride fares.
- Retrieve ride details and rider ride history.
- Start, complete, and cancel rides.
- Manage ride status transitions.

### 2. Real-Time Driver Location
- Store driver coordinates using Redis GEO.
- Update driver locations.
- Search for nearby drivers using geographic radius queries.
- Retrieve nearby drivers with distance information.
- Remove driver locations when drivers go offline.

### 3. Driver Matching
- Process ride requests asynchronously using Kafka.
- Retrieve nearby drivers through OpenFeign.
- Select a suitable driver based on distance and simulated driver rating.
- Publish driver-matching results through Kafka.

### 4. Microservices Communication
- Kafka for asynchronous event-driven communication.
- OpenFeign for synchronous service-to-service communication.
- Docker Compose for running services and infrastructure together.

## End-to-End Ride Workflow

1. A rider submits a ride request to the Ride Service.
2. Ride Service validates the request and persists it in PostgreSQL.
3. Ride Service publishes a `RideRequestedEvent` to the `ride.requested` Kafka topic.
4. Matching Service consumes the event.
5. Matching Service calls Location Service through OpenFeign.
6. Location Service searches Redis GEO for nearby drivers.
7. Matching Service evaluates available drivers and selects a suitable candidate.
8. Matching Service publishes a `RideMatchedEvent` to the `ride.matched` Kafka topic.
9. Ride Service consumes the event, assigns the selected driver, and updates the ride status.

**Driver selection:** The current implementation uses a weighted scoring approach based on distance (70%) and simulated driver rating (30%). A dedicated Driver Service is not yet implemented.

### Ride Status Lifecycle

```text
REQUESTED
    |
    v
 MATCHING
    |
    v
 ACCEPTED
    |
    v
 RIDE_STARTED
    |
    v
 COMPLETED
```

A ride can also transition to `CANCELLED` from supported earlier states.

## Project Structure

```text
RideFlow/
│
├── docker-compose.yml
│
├── ride-service/
│   ├── Dockerfile
│   └── ride-service/
│       ├── src/
│       ├── pom.xml
│       └── mvnw
│
├── location-service/
│   ├── Dockerfile
│   └── location-service/
│       ├── src/
│       ├── pom.xml
│       └── mvnw
│
├── matching-service/
│   ├── Dockerfile
│   └── matching-service/
│       ├── src/
│       ├── pom.xml
│       └── mvnw
│
└── README.md
```

## Getting Started

### Prerequisites

- Java Development Kit (JDK 21 or compatible)
- Docker Desktop
- Docker Compose
- Git

Verify your installations:

```bash
java -version
docker --version
docker compose version
```

### Run the Complete Application Using Docker

Clone the repository:

```bash
git clone https://github.com/Vishwanath-B-M/Ride-flow.git

cd Ride-flow
```

The current Docker Compose configuration uses an external PostgreSQL volume. Create it before the first startup if it does not already exist:

```bash
docker volume create uber-app_postgres-data
```

Build and start the application:

```bash
docker compose up --build -d
```

Check running containers:

```bash
docker compose ps
```

Check application logs:

```bash
docker compose logs -f ride-service
docker compose logs -f location-service
docker compose logs -f matching-service
```

### Service Endpoints

| Component | URL |
|---|---|
| Ride Service | http://localhost:8083 |
| Location Service | http://localhost:8082 |
| Matching Service | http://localhost:8084 |
| PostgreSQL | localhost:5432 |
| Redis | localhost:6379 |
| Kafka | localhost:9092 |

### Health Checks

```bash
curl http://localhost:8083/actuator/health

curl http://localhost:8082/actuator/health

curl http://localhost:8084/actuator/health
```

### Stop the Application

```bash
docker compose stop
```

To stop and remove containers and the Compose network:

```bash
docker compose down
```

The PostgreSQL external volume is preserved by these commands. Avoid `docker compose down -v` if you want to retain your database.

## Running Services Locally

For development, you can run the infrastructure using Docker:

```bash
docker compose up -d postgres redis zookeeper kafka
```

Then run each Spring Boot application in a separate terminal.

**Ride Service**

```bash
cd ride-service/ride-service
./mvnw spring-boot:run
```

**Location Service**

```bash
cd location-service/location-service
./mvnw spring-boot:run
```

**Matching Service**

```bash
cd matching-service/matching-service
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

## API Reference

### Ride Service

Base URL: `http://localhost:8083/api/v1/rides`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/request` | Create a ride request |
| GET | `/{rideId}` | Retrieve ride details |
| GET | `/rider/{riderId}` | Retrieve rider ride history |
| PUT | `/{rideId}/start` | Start a ride |
| PUT | `/{rideId}/complete` | Complete a ride |
| PUT | `/{rideId}/cancel` | Cancel a ride |

### Location Service

Base URL: `http://localhost:8082/api/v1/locations`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/drivers/update` | Update driver location |
| GET | `/driver/nearby` | Find nearby drivers |
| DELETE | `/drivers/{driverId}` | Remove driver location |

## Example API Requests

### 1. Update Driver Location

```http
POST http://localhost:8082/api/v1/locations/drivers/update
Content-Type: application/json

{
  "driverId": "driver:4",
  "latitude": 26.9125,
  "longitude": 75.7874
}
```

### 2. Find Nearby Drivers

```http
GET http://localhost:8082/api/v1/locations/driver/nearby?latitude=26.9124&longitude=75.7873&radius=5.0
```

### 3. Request a Ride

```http
POST http://localhost:8083/api/v1/rides/request
Content-Type: application/json

{
  "riderId": "101",
  "pickupLatitude": 26.9124,
  "pickupLongitude": 75.7873,
  "pickupAddress": "Jaipur Railway Station",
  "dropLatitude": 26.8467,
  "dropLongitude": 75.8056,
  "dropAddress": "Malviya Nagar, Jaipur"
}
```

## Kafka Topics

| Topic | Producer | Consumer | Purpose |
|---|---|---|---|
| `ride.requested` | Ride Service | Matching Service | Process new ride requests |
| `ride.matched` | Matching Service | Ride Service | Process driver assignments |

## Redis GEO

Driver coordinates are stored in Redis using geospatial indexing.

Location Service uses Redis GEO operations to:
- Add and update driver coordinates.
- Search for drivers within a specified radius.
- Calculate distances between drivers and pickup locations.
- Retrieve nearby drivers ordered by distance.

## Engineering Concepts Demonstrated

- Microservices architecture
- Event-driven architecture
- Asynchronous communication with Apache Kafka
- Synchronous communication with Spring Cloud OpenFeign
- Geospatial indexing and proximity search with Redis
- Relational database persistence with PostgreSQL
- REST API design
- Containerization using Docker
- Multi-container orchestration using Docker Compose
- Service health monitoring using Spring Boot Actuator

## Future Improvements

- Dedicated Driver Service with driver profiles and real ratings.
- Driver availability and accept/reject workflows.
- Authentication and authorization.
- API Gateway and service discovery.
- Resilience using circuit breakers and retry mechanisms.
- Kafka retry handling and dead-letter topics.
- Integration testing with Testcontainers.
- Centralized logging and distributed tracing.
- Prometheus and Grafana monitoring.
- Kubernetes deployment.
- CI/CD automation.

---

**Author:** Vishwanath Basavaraj Mirji

**GitHub:** [Vishwanath-B-M](https://github.com/Vishwanath-B-M)

**Repository:** [RideFlow](https://github.com/Vishwanath-B-M/Ride-flow)