# URL Shortener

Spring Boot URL shortener with PostgreSQL persistence, Redis caching, Redis-backed analytics, redirect rate limiting, and a single-page frontend dashboard.

## Stack

- Java 21
- Spring Boot 4
- Spring Web
- Spring Data JPA
- PostgreSQL
- Redis
- Maven Wrapper
- Plain HTML, CSS, and JavaScript frontend

## Features

- Create, update, delete, and resolve short URLs
- Short codes generated with a clean 7-character alphabet
- Redis cache for fast short-code resolution
- Redis analytics for redirect counts
- Per-short-code rate limiting
- Expiring links with countdowns in the dashboard
- Analytics dashboard with auto-refresh

## Requirements

- JDK 21 or later
- PostgreSQL running locally
- Redis running locally

## Configuration

Application settings are in [application.properties](src/main/resources/application.properties).

Important properties:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.data.redis.host`
- `spring.data.redis.port`
- `app.base-url`
- `app.display-base-url`

`app.base-url` is the actual backend URL used for working redirects.

`app.display-base-url` is the clean public-looking host shown in the UI, for example `trim.ly`.

## Run

Build and run with the Maven wrapper:

```bash
./mvnw clean package -DskipTests
java -jar target/url-shortener-0.0.1-SNAPSHOT.jar
```

On Windows PowerShell:

```powershell
.\mvnw.cmd clean package -DskipTests
java -jar target\url-shortener-0.0.1-SNAPSHOT.jar
```

Open:

```text
http://localhost:8080
```

## API

- `POST /shorten`
- `GET /{shortCode}`
- `PUT /{shortCode}`
- `DELETE /{shortCode}`
- `GET /api/config`
- `GET /api/analytics`
- `POST /api/analytics/query`
- `GET /api/analytics/{shortCode}`

## Notes

- PostgreSQL stores the durable URL records.
- Redis stores cache entries, redirect counters, and rate-limit keys.
- Existing database rows created before `createdAt` and `expiresAt` were introduced can still appear with `null` values until updated or recreated.
