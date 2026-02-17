# Land Routing Service

## Overview

The **Land Routing Service** is a Spring Boot application that calculates the shortest land route between two countries based on their border information.
It uses the [countries.json dataset](https://raw.githubusercontent.com/mledoze/countries/master/countries.json) and exposes a REST API endpoint for route lookup.

The service handles:

- Efficient shortest-path calculation (BFS)
- Validation of country codes (CCA3)
- Graceful fallback if external JSON is unavailable
- OpenAPI/Swagger documentation

---

## Features

- Spring Boot + Gradle + Java 24
- REST endpoint: `/routing/{origin}/{destination}`
- Returns the shortest land route if available
- HTTP 400 if no land route exists
- CCA3 country code identification
- Swagger UI at `/swagger-ui.html`
- Fallback to local JSON copy if the remote source is unavailable
- Unit & integration tests included

---

## Installation

### Prerequisites

- Java 24
- Gradle 8+
- (Optional) Docker if you want to run in a container

### Build

```bash
./gradlew clean build
```

This produces a JAR in build/libs/.

### Running

Run locally:

```bash
java -jar build/libs/CountryRoutingPWC-0.0.1-SNAPSHOT.jar
```

The application will start on port 8080.

## Running via Docker

This project includes a **multi-stage Dockerfile** that builds the fat JAR and runs it in a container.

### Dockerfile

```dockerfile
# Stage 1: Build the fat JAR using Gradle wrapper
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Copy Gradle wrapper and version catalog
COPY gradle ./gradle
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .
COPY gradle/libs.versions.toml ./gradle/libs.versions.toml

# Copy source
COPY src ./src

# Make gradlew executable
RUN chmod +x ./gradlew

# Build fat JAR (includes Springdoc)
RUN ./gradlew clean bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy the fat JAR from builder stage
COPY --from=builder /app/build/libs/CountryRoutingPWC-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
```

### Build Docker image

```bash
docker build -t country-routing .
```

### Run the container

```bash
docker run -p 8080:8080 country-routing
```

- The `-p 8080:8080` maps the container port to your host machine.
- The service will be available at:
    - REST endpoint: `http://localhost:8080/routing/{origin}/{destination}`
    - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- The fat JAR includes all dependencies, including Springdoc, so Swagger UI works inside the container. 

## API Usage

Endpoint:

GET /routing/{origin}/{destination}

**Parameters:**

| Name        | Description                   | Example |
|-------------|-------------------------------|---------|
| origin      | Origin country CCA3 code      | CZE     |
| destination | Destination country CCA3 code | ITA     |

**Success Response (200):**

```json
{
  "route": [
    "CZE",
    "AUT",
    "ITA"
  ]
}
```

**Failure Response (400):**

No land route exists or invalid country code

## Swagger / OpenAPI

Swagger UI is available at:

```bash
http://localhost:8080/swagger-ui/index.html
```

It documents all endpoints, request parameters, and response schemas (using RouteResponse).

## Configuration

The application reads the countries dataset URL from application.yml:

```yaml
app:
  countries:
    url: "https://raw.githubusercontent.com/mledoze/countries/master/countries.json"
```

If the URL is unavailable, a local fallback copy from resources/countries.json is used.

## Testing

Run all tests:

```bash
./gradlew test
```

Included:
- Integration tests for /routing endpoint using MockMvc
- Unit tests for BFS shortest path logic
- Edge case tests for invalid country codes or unreachable routes

## Notes about BFS 

### How BFS works (step by step)

Imagine you want a route from CZE to ITA.

1. Initialization:
    - Create a queue to store countries to visit.
    - Start with CZE in the queue.
    - Keep a map of visited countries to avoid revisiting.
    - Optionally, keep a parent map to reconstruct the route.
2. Loop until the queue is empty:
    - Take the first country from the queue (current).
    - Check if current is the destination. If yes, stop.
    - Otherwise, add all neighboring countries (from borders) not yet visited to the queue.
    - Mark them as visited.
    - Record that they were reached from current (to reconstruct the path later).
3. Reconstruct path:
    - Start from the destination.
    - Move backward using the parent map until you reach the origin.
    - Reverse the list to get the correct route.

### Why BFS is optimal here

- Every border = 1 step → BFS guarantees minimum number of borders.
- Graph is small and sparse → BFS completes in milliseconds.
- Memory usage is minimal: only queue + visited set + parent map.
- Easy to implement and maintain.

## Other Algorithm Options

| Algorithm            | Pros                                         | Cons                                         | Suitability for Country Routing                       |
|----------------------|---------------------------------------------|---------------------------------------------|------------------------------------------------------|
| **BFS**              | Simple, finds shortest path in unweighted graphs, very fast for small graphs | None significant here                        | ✅ Best choice                                       |
| **DFS**              | Simple, explores all paths                  | Does **not guarantee shortest path**         | ❌ Not suitable                                     |
| **Dijkstra**         | Handles weighted graphs                      | Slower than BFS for unweighted graphs       | ⚠️ Overkill (weights = 1)                           |
| **A***              | Fast if good heuristic exists                | Needs heuristic (distance), more complex    | ⚠️ Only useful with weights/heuristic              |
| **Floyd-Warshall**   | Precomputes all pairs                        | O(V³), high memory                           | ❌ Overkill for ~200 nodes                           |
| **Bidirectional BFS** | Can halve search space                     | Slightly more complex                        | ✅ Optional optimization, can improve BFS slightly  |

## Dev notes

- Two hours of development. 
- Most of the manual work was spent on setting up the project and its structure, making sure everything runs smoothly.   
- AI picked the correct algorithm, mostly did a good job at covering the whole use-case.
- I've prompted for Swagger, cleaner DTO, json fallback and testing.
- Originally I had this set up for Java 24 as I use that on my private projects. 
However, I decided to switch to Java 17 for this one 'cause docker kept complaining and I didn't want to spend more time on it.
