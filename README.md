# JUNYOUNG-LLM-BACKEND

A REST API for placing, taking, and listing delivery orders.

## How to Run

Docker is prerequisite.

```bash

./start.sh
```

## How to run test

```bash
./gradlew test
```

## Setup

A `GOOGLE_MAPS_API_KEY` is required to use the Google Maps API.

Create `.env.local` in the project root and set the key as the value for the `GOOGLE_MAPS_API_KEY` environment variable. (`start.sh` _is injecting_ `.env.local` _into the container._)

```bash
export GOOGLE_MAPS_API_KEY=your_actual_api_key
# or
cp .env.example .env.local

./start.sh
```

## Notes on implementation

The code is organized feature-based: order and distance.

The order directory contains the MVC components including `OrderController,` `OrderService`, `OrderRepository`, relevant entities and DTOs.

The distance directory contains separated classes and beans with responsibility to return a distance between two coordinates points when requested. In such way, it's implementation could be cohesive and also can be interchanged - `FakeDistanceService` and `GoogleAPIDistanceService` calling Google Maps API.

- When a google maps api key is provided from environment variable, Google API is used automatically.

The requirement stated was to handle potential concurrency issue. Hence, the query was written considering that two clients may request to take a same order at the same time.

- To solve the issue, the 'take order' query is designed to be an atomic update that only updates the order status to "TAKEN" if the order is not already taken (UNASSIGNED). Even when the two requests are sent at the same time, only the first request will succeed and the second request will update 0 rows, resulting in business validation logic throwing an exception.
- To reproduce the edge case, it was tested by manually triggering long latency, and also an integration test simulating concurrent requests scenario.

Unit tests are written with focus on the validations:

- on OrderController focuses on HTTP requests' input validation and relevant response data.
- on OrderService covers business logic validation.
  - Such as order cannot be taken if its already taken.

Integration tests (`OrderControllerIntegrationTest`) are written for a happy path and some failing paths using test containers and running DB.

### Tech Stack

- Java 17
- Spring Boot 4 & Spring Data JPA
- PostgreSQL 18
- Flyway
- Gradle
- Testcontainers
