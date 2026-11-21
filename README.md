# FleetMind – Agentic AI-Powered Distributed Mobility Platform

FleetMind is an event-driven,(4 services) microservices-based ride-hailing backend built using **Java, Spring Boot, Apache Kafka, Redis, PostgreSQL, Docker, and Spring AI**.

The platform manages ride requests, driver discovery, matching workflows, and ride lifecycles. It also features an **Agentic AI operations investigation service** that uses LLM-powered tool calling to collect evidence from multiple microservices, investigate ride incidents, identify likely root causes, and generate structured operational reports.

## Architecture

FleetMind consists of four independent Spring Boot services:

| Service | Port | Responsibility |
|---|---:|---|
| Ride Service | 8083 | Ride creation, lifecycle management, and ride information |
| Location Service | 8082 | Driver location updates and nearby-driver discovery |
| Matching Service | 8084 | Driver selection and matching-attempt tracking |
| Agent Service | 8085 | AI-powered ride investigation and operational analysis |

### Architecture Overview

```text
                         Client
                           |
                           v
                     Ride Service
                        :8083
                           |
                    Kafka: ride.requested
                           |
                           v
                   Matching Service
                        :8084
                      /        \
                     v          v
              Location Service  Kafka
                   :8082       ride.matched
                     |             |
                     v             v
                  Redis GEO    Ride Service
                     
                     
                   Agent Service
                      :8085
                   /     |      \
                  v      v       v
                Ride  Matching  Location
               Service Service  Service
```

## Key Features

### 1. Microservices Architecture

- Developed four Spring Boot microservices with independent responsibilities.
- Implemented REST APIs for ride management, driver locations, matching, and investigations.
- Used Spring Cloud OpenFeign for synchronous inter-service communication.
- Separated business responsibilities across services.

### 2. Event-Driven Communication with Kafka

- Integrated Apache Kafka for asynchronous communication between services.
- Published `ride.requested` events when a ride is created.
- Published `ride.matched` events after successful driver matching.
- Implemented Kafka consumers to process ride-matching events.

### 3. Driver Location Management with Redis GEO

- Integrated Redis GEO for storing and discovering driver locations.
- Implemented driver location updates using latitude and longitude.
- Developed proximity-based nearby-driver discovery.
- Retrieved nearby drivers along with their calculated distances.

### 4. Driver Matching and Attempt Tracking

- Implemented a driver-matching workflow using nearby-driver information.
- Persisted matching attempts in PostgreSQL.
- Recorded matching status, nearby-driver count, search radius, selected driver, and failure reason.
- Exposed REST APIs to retrieve matching history and the latest attempt.
- Handled unsuccessful matching attempts when no nearby drivers were available.

### 5. Ride Lifecycle and Concurrency Control

- Implemented ride creation, retrieval, fare estimation, and lifecycle management.
- Managed ride states including `MATCHING`, `ACCEPTED`, `RIDE_STARTED`, `COMPLETED`, and `CANCELLED`.
- Implemented JPA version-based optimistic locking to prevent multiple drivers from successfully accepting the same ride concurrently.
- Validated concurrent ride acceptance using an integration test, ensuring only one competing update succeeds.

### 6. Agentic AI Operations Investigation

- Integrated Spring AI with the Groq API for LLM-powered investigation.
- Implemented Java tools using Spring AI tool-calling annotations.
- Enabled the AI agent to retrieve evidence from Ride, Matching, and Location services.
- Used LLM-driven tool calling to investigate ride status, matching attempts, and nearby-driver availability.
- Generated structured investigation reports containing severity, summary, likely root cause, supporting evidence, and recommendations.
- Tested investigations involving both successful and failed driver-matching scenarios.

**Example investigation capabilities:**
- Investigate why a ride was not matched.
- Check whether nearby drivers were available.
- Examine matching-attempt history and failure reasons.
- Correlate information from multiple services.
- Generate evidence-based operational recommendations.

### 7. Containerization and Testing

- Configured supporting infrastructure and services using Docker Compose.
- Containerized application services and infrastructure components.
- Executed tests across all four services.

**Test results:**

| Service | Tests | Failures | Errors |
|---|---:|---:|---:|
| Ride Service | 13 | 0 | 0 |
| Matching Service | 1 | 0 | 0 |
| Agent Service | 1 | 0 | 0 |
| Location Service | 1 | 0 | 0 |
| **Total** | **16** | **0** | **0** |

## Technology Stack

| Category | Technologies |
|---|---|
| Language | Java |
| Backend | Spring Boot, Spring Web, Spring Data JPA |
| AI Integration | Spring AI, Groq API, LLM Tool Calling |
| Messaging | Apache Kafka, Zookeeper |
| Database | PostgreSQL |
| Geospatial Data | Redis GEO |
| Service Communication | REST APIs, Spring Cloud OpenFeign |
| Concurrency Control | JPA Optimistic Locking |
| Containerization | Docker, Docker Compose |
| Build Tool | Maven |

## API Endpoints

### Ride Service – Port 8083

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/rides/request` | Create a ride request |
| GET | `/api/v1/rides/{rideId}` | Retrieve ride details |

### Location Service – Port 8082

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/locations/drivers/update` | Update driver location |
| GET | `/api/v1/locations/driver/nearby` | Discover nearby drivers |

Nearby-driver query parameters:
- `latitude`
- `longitude`
- `radius`

### Matching Service – Port 8084

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/matching/attempts/{rideId}` | Retrieve matching attempts |
| GET | `/api/v1/matching/attempts/{rideId}/latest` | Retrieve the latest matching attempt |

### Agent Service – Port 8085

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/agent/rides/{rideId}` | Retrieve ride details through Agent Service |
| POST | `/api/agent/investigations` | Run evidence-based ride investigation |
| POST | `/api/agent/investigations/ai` | Run LLM-powered AI investigation |

## Example Requests

### 1. Request a Ride

```bash
curl -X POST http://localhost:8083/api/v1/rides/request \
-H "Content-Type: application/json" \
-d '{
  "riderId": "rider-101",
  "pickupLatitude": 12.9716,
  "pickupLongitude": 77.5946,
  "pickupAddress": "MG Road, Bengaluru",
  "dropLatitude": 12.9352,
  "dropLongitude": 77.6245,
  "dropAddress": "Indiranagar, Bengaluru"
}'
```

### 2. Run an Evidence-Based Investigation

```bash
curl -X POST http://localhost:8085/api/agent/investigations \
-H "Content-Type: application/json" \
-d '{
  "rideId": "30",
  "question": "Investigate this ride and identify any matching problems."
}'
```

### 3. Run an Agentic AI Investigation

```bash
curl -X POST http://localhost:8085/api/agent/investigations/ai \
-H "Content-Type: application/json" \
-d '{
  "rideId": "30",
  "question": "Investigate this ride. Check its status, matching attempts and nearby drivers. Explain whether there is any operational issue."
}'
```

The AI agent uses its Java tools to gather information from the relevant services before generating an investigation report.

## Verified Scenarios

### Successful Driver Matching

Tested a ride where a nearby driver was available.

The investigation retrieved:
- Ride status: `ACCEPTED`
- Assigned driver: `D102`
- Matching result: `SUCCESS`
- Nearby drivers: 2

The AI investigation successfully collected the information and reported that no operational issue was identified.

### Failed Driver Matching

Tested a ride where no nearby drivers were available.

The matching service recorded:
- Matching status: `FAILED`
- Nearby driver count: `0`
- Failure reason: `NO_DRIVERS_AVAILABLE`
- Search radius: `5.0 km`
- Selected driver: `null`

The AI agent successfully investigated the ride, identified the lack of nearby drivers as the likely root cause, and generated recommendations.

### Concurrent Driver Acceptance

Tested concurrent ride-acceptance updates using JPA optimistic locking.

The integration test verified that only one competing update succeeds, preventing two drivers from accepting the same ride through concurrent database updates.

## Running the Project

### Prerequisites

- JDK 21 or compatible Java installation
- Docker Desktop
- Maven
- Git
- Groq API key

### 1. Clone the Repository

```bash
git clone https://github.com/Vishwanath-B-M/FleetMind.git
cd FleetMind
```

### 2. Start Docker Services

```bash
docker compose up -d --build
```

This starts the services and supporting infrastructure configured in Docker Compose.

### 3. Configure the Groq API Key

Set your API key as an environment variable:

```bash
export GROQ_API_KEY="your_groq_api_key"
```

Never commit API keys or secrets to the repository.

### 4. Configure Agent Service

The Agent Service uses the following configuration in `application.properties`:

```properties
spring.application.name=agent-service
server.port=8085

spring.ai.openai.api-key=${GROQ_API_KEY}
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.chat.options.model=openai/gpt-oss-120b

services.ride.url=http://localhost:8083
services.location.url=http://localhost:8082
services.matching.url=http://localhost:8084
```

### 5. Start Agent Service

From the `agent-service` directory:

```bash
./mvnw spring-boot:run
```

Ensure the `GROQ_API_KEY` environment variable is available in the terminal session.

## Current Limitations

- A failed matching attempt can leave the ride status as `MATCHING`; synchronizing the ride lifecycle with matching failures remains an improvement.
- Additional automated end-to-end tests across all microservices would strengthen integration coverage.
- Service observability, retry handling, and failure recovery can be further improved.

## Future Improvements

- Synchronize ride lifecycle transitions with matching outcomes.
- Expand automated integration and end-to-end test coverage.
- Improve service observability and failure recovery.
- Extend operational investigation and incident-reporting capabilities.

## Author

**Vishwanath Basavaraj Mirji**

B.Tech – Computer Science and Engineering (2023–2027)

GitHub: [Vishwanath-B-M](https://github.com/Vishwanath-B-M)
