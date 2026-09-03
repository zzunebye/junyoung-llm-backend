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
./start.sh
```

## Tech Stack

- Java 17
- Spring Boot 4 & Spring Data JPA
- PostgreSQL 18
- Flyway
- Gradle
- Testcontainers
