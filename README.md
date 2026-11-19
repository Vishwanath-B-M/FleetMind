# # FleetMind – Agentic AI-Powered Distributed Mobility Platform

RideFlow is an event-driven, microservices-based ride-hailing backend built using **Java, Spring Boot, Apache Kafka, Redis, PostgreSQL, Docker, and Spring AI**.

The platform demonstrates how independent backend services communicate to process ride requests, discover nearby drivers, perform driver matching, and manage ride lifecycles.

It also includes an **Agentic AI Operations Service** that uses LLM-powered tool calling to investigate ride incidents across multiple microservices, collect operational evidence, identify likely root causes, and generate recommendations.

## Architecture

```text
                       Rider Application
                              |
                              | HTTP REST
                              v
                    +--------------------+
                    |    Ride Service    |
                    |       :8083        |
                    |                    |
                    | Ride Management    |
                    | PostgreSQL         |
                    +---------+----------+
                              |
                              | Kafka: ride.requested
                              v
                    +--------------------+
                    |  Matching Service  |
                    |       :8084        |
                    |                    |
                    | Driver Matching    |
                    | Matching Attempts  |
                    +---------+----------+
                              |
                              | OpenFeign
                              v
                    +--------------------+
                    |  Location Service  |
                    |       :8082        |
                    |                    |
                    | Redis GEO Search   |
                    +---------+----------+
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


                  +------------------------+
                  |      Agent Service     |
                  |          :8085          |
                  |                        |
                  |  Spring AI / ChatClient|
                  |  LLM Tool Calling      |
                  +-----------+------------+
                              |
                    +---------+---------+
                    |         |         |
                    v         v         v
                 Ride     Matching   Location
                Service   Service    Service
                 Tools     Tools      Tools
                    \         |         /
                     \        |        /
                      +-------+-------+
                              |
                              v
                    Evidence-Based AI
                    Investigation Report
```

## Microservices

| Service | Port | Responsibility | Technology |
|---|---:|---|---|
| Ride Service | 8083 | Ride requests, lifecycle, fare estimation and persistence | Spring Boot, PostgreSQL |
| Location Service | 8082 | Driver location updates and nearby-driver discovery | Spring Boot, Redis GEO |
| Matching Service | 8084 | Asynchronous driver matching and matching-attempt tracking | Spring Boot, Kafka, OpenFeign, PostgreSQL |
| Agent Service | 8085 | AI-powered ride investigations and operational analysis | Spring Boot, Spring AI, LLM Tool Calling |

## Tech Stack

**Backend**
- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Data Redis
- Spring Kafka
- Spring Cloud OpenFeign
- Spring AI
- Spring Boot Actuator
- REST APIs

**Databases and Messaging**
- PostgreSQL
- Redis GEO
- Apache Kafka
- Apache ZooKeeper

**AI Integration**
- Spring AI ChatClient
- LLM-powered tool calling
- Groq API with OpenAI-compatible integration
- Evidence-based operational investigations

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
- Record matching attempts in PostgreSQL.
- Track matching outcomes, nearby-driver counts, search radius, selected driver, and failure reasons.

**Driver selection:** The current implementation uses a weighted scoring approach based on distance (70%) and simulated driver rating (30%). A dedicated Driver Service is not yet implemented.

### 4. Agentic AI Operations and Ride Investigation

RideFlow includes an AI-powered operations agent built using Spring AI.

Instead of relying on hardcoded investigation results, the LLM can invoke registered Java tools to retrieve operational information from the existing microservices.

**Registered AI tools:**

| Tool | Responsibility |
|---|---|
| Ride Investigation Tool | Retrieves ride details and current status |
| Matching Investigation Tool | Retrieves matching attempts and failure information |
| Location Investigation Tool | Retrieves nearby drivers around the pickup location |

**Investigation workflow:**

1. An operator submits a ride ID and investigation question.
2. Agent Service sends the request to the LLM through Spring AI ChatClient.
3. The LLM can invoke the registered investigation tools.
4. The tools retrieve actual information from Ride, Matching, and Location Services.
5. The LLM analyzes the collected evidence.
6. The agent generates an operational investigation report.

**Investigation reports include:**
- Ride status and driver assignment.
- Matching-attempt history.
- Nearby-driver availability.
- Investigation severity.
- Likely root cause.
- Supporting operational evidence.
- Recommended corrective actions.

**Example investigations validated:**

**Successful matching – Ride 30**
- Ride status: `ACCEPTED`.
- Selected driver: `D102`.
- Nearby drivers found: 2.
- Matching result: `SUCCESS`.
- AI investigation correctly identified that the ride had been matched successfully.

**Failed matching – Ride 31**
- Ride status: `MATCHING`.
- Nearby drivers found: 0.
- Matching result: `FAILED`.
- Failure reason: `NO_DRIVERS_AVAILABLE`.
- Search radius: 5 km.
- AI investigation correctly identified the lack of nearby drivers as the root cause and recommended corrective actions.

These scenarios demonstrate the agent's ability to retrieve evidence from multiple services and generate context-aware operational reports.

### 5. Microservices Communication

- Kafka for asynchronous event-driven communication.
- OpenFeign for synchronous service-to-service communication.
- REST APIs for service interactions.
- Docker Compose for running the core services and infrastructure together.

## End-to-End Ride Workflow

1. A rider submits a ride request to the Ride Service.
2. Ride Service validates the request and persists it in PostgreSQL.
3. Ride Service publishes a `RideRequestedEvent` to the `ride.requested` Kafka topic.
4. Matching Service consumes the event.
5. Matching Service calls Location Service through OpenFeign.
6. Location Service searches Redis GEO for nearby drivers.
7. Matching Service evaluates available drivers and selects a suitable candidate.
8. Matching Service records the matching attempt in PostgreSQL.
9. Matching Service publishes a `RideMatchedEvent` to the `ride.matched` Kafka topic.
10. Ride Service consumes the event, assigns the selected driver, and updates the ride status.

## AI Investigation Workflow

```text
        Operator Investigation Request
                    |
                    v
              Agent Service
                    |
                    v
          Spring AI ChatClient
                    |
                    v
             LLM Reasoning
                    |
           Selects Relevant Tools
                    |
          +---------+---------+
          |         |         |
          v         v         v
        Ride    Matching   Location
        Tool      Tool       Tool
          |         |         |
          v         v         v
        Ride    Matching   Nearby
        Data    Attempts   Drivers
          \         |         /
           \        |        /
            +-------+-------+
                    |
                    v
             Evidence Analysis
                    |
                    v
          Operational Investigation
                  Report
```

The LLM uses the registered tools to obtain actual service data. The final report is generated from the retrieved evidence rather than a predefined investigation response.

## Ride Status Lifecycle

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

**Current limitation:** When a matching attempt fails because no drivers are available, Matching Service correctly records the failure in PostgreSQL. However, the Ride Service currently leaves the ride in `MATCHING`. Synchronizing the ride lifecycle with matching failures is a future improvement.

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
├── agent-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/rideshare/agent/
│   │       │       ├── controller/
│   │       │       ├── service/
│   │       │       ├── tools/
│   │       │       └── ...
│   │       └── resources/
│   │           └── application.properties
│   ├── pom.xml
│   └── mvnw
│
└── README.md
```

## Getting Started

### Prerequisites

- Java Development Kit (JDK 21 or compatible).
- Docker Desktop.
- Docker Compose.
- Git.
- Groq API key for AI investigation functionality.

Verify your installations:

```bash
java -version
docker --version
docker compose version
```

### Run the Core Application Using Docker

Clone the repository:

```bash
git clone https://github.com/Vishwanath-B-M/Ride-flow.git

cd Ride-flow
```

The current Docker Compose configuration uses an external PostgreSQL volume. Create it before the first startup if it does not already exist:

```bash
docker volume create uber-app_postgres-data
```

Build and start the core services and infrastructure:

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

**Note:** Agent Service is currently run separately using Spring Boot, rather than as part of the Docker Compose application.

### Run Agent Service Locally

Open a separate terminal:

```bash
cd agent-service
```

Set your Groq API key as an environment variable:

```bash
read -s "GROQ_API_KEY?Paste your Groq API key: "
export GROQ_API_KEY
```

The key is read into the terminal environment and should not be committed to GitHub.

The Agent Service uses the following configuration in `application.properties`:

```properties
spring.application.name=agent-service
server.port=8085

spring.ai.openai.api-key=${GROQ_API_KEY}
spring.ai.openai.base-url=https://api.groq.com/openai/v1
spring.ai.openai.chat.options.model=openai/gpt-oss-20b
spring.ai.openai.chat.replay-reasoning-content=false

services.ride.url=http://localhost:8083
services.location.url=http://localhost:8082
services.matching.url=http://localhost:8084
```

Start Agent Service:

```bash
./mvnw spring-boot:run
```

Make sure the Ride, Location, and Matching services are running before performing AI investigations.

## Service Endpoints

| Component | URL |
|---|---|
| Ride Service | `http://localhost:8083` |
| Location Service | `http://localhost:8082` |
| Matching Service | `http://localhost:8084` |
| Agent Service | `http://localhost:8085` |
| PostgreSQL | `localhost:5432` |
| Redis | `localhost:6379` |
| Kafka | `localhost:9092` |

## Health Checks

```bash
curl http://localhost:8083/actuator/health

curl http://localhost:8082/actuator/health

curl http://localhost:8084/actuator/health
```

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

### Matching Service

Base URL: `http://localhost:8084/api/v1/matching`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/attempts/{rideId}` | Retrieve all matching attempts for a ride |
| GET | `/attempts/{rideId}/latest` | Retrieve the latest matching attempt |

### Agent Service

Base URL: `http://localhost:8085/api/agent/investigations`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/` | Perform a deterministic ride investigation |
| POST | `/ai` | Perform an LLM-powered investigation using tool calling |

## Example API Requests

### 1. Update Driver Location

```http
POST http://localhost:8082/api/v1/locations/drivers/update
Content-Type: application/json

{
  "driverId": "driver-101",
  "latitude": 12.9720,
  "longitude": 77.5950
}
```

### 2. Find Nearby Drivers

```http
GET http://localhost:8082/api/v1/locations/driver/nearby?latitude=12.9716&longitude=77.5946&radius=5.0
```

### 3. Request a Ride

```http
POST http://localhost:8083/api/v1/rides/request
Content-Type: application/json

{
  "riderId": "rider-101",
  "pickupLatitude": 12.9716,
  "pickupLongitude": 77.5946,
  "pickupAddress": "MG Road, Bengaluru",
  "dropLatitude": 12.9352,
  "dropLongitude": 77.6245,
  "dropAddress": "Indiranagar, Bengaluru"
}
```

### 4. Retrieve Matching Attempts

```bash
curl http://localhost:8084/api/v1/matching/attempts/30
```

### 5. Perform an AI-Powered Ride Investigation

```bash
curl -X POST http://localhost:8085/api/agent/investigations/ai \
-H "Content-Type: application/json" \
-d '{
  "rideId": "30",
  "question": "Investigate this ride. Check its status, matching attempts and nearby drivers. Explain whether there is any operational issue."
}'
```

### 6. Investigate a Failed Ride

```bash
curl -X POST http://localhost:8085/api/agent/investigations/ai \
-H "Content-Type: application/json" \
-d '{
  "rideId": "31",
  "question": "Investigate why this ride failed. Check its ride status, matching attempts and nearby drivers. Identify the root cause and recommend corrective actions."
}'
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
- Remove driver locations when drivers go offline.

## Engineering Concepts Demonstrated

- Microservices architecture.
- Event-driven architecture.
- Asynchronous communication with Apache Kafka.
- Synchronous service-to-service communication with OpenFeign.
- Geospatial indexing and proximity search with Redis.
- Relational database persistence with PostgreSQL.
- Matching-attempt tracking and failure diagnosis.
- REST API design.
- LLM-powered Agentic AI using Spring AI.
- Function/tool calling across independent microservices.
- Evidence-based operational investigation.
- Containerization using Docker.
- Multi-container orchestration using Docker Compose.
- Service health monitoring using Spring Boot Actuator.

## Future Improvements

- Synchronize ride lifecycle states with matching failures.
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
- Improved AI error handling and investigation validation.

---

**Author:** Vishwanath Basavaraj Mirji

**GitHub:** [Vishwanath-B-M](https://github.com/Vishwanath-B-M)

**Repository:** [RideFlow](https://github.com/Vishwanath-B-M/Ride-flow)
