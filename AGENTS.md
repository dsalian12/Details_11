# Agent Guidelines for Product API

This repository contains `product-api`, a Spring Boot 3 REST API for managing a `Product` catalog. It is packaged as a Maven project with container support. This file provides the conventions and commands used in this specific project.

## Application Overview

- **Name:** product-api
- **Group:** com.salian
- **Purpose:** CRUD REST API for `Product` resources (sku, name, description, price, quantity), backed by PostgreSQL, documented with OpenAPI/Swagger.
- **Base API path:** `/api/v1/products`
- **Endpoints:** GET (paginated), GET by id, POST, PUT, DELETE
- **Errors:** Returned as RFC 7807 `ProblemDetail` (400 validation, 404 not found, 409 duplicate SKU).
- **Health:** `/actuator/health`; Swagger UI: `/swagger-ui.html`; OpenAPI JSON: `/v3/api-docs`.

## Technology Stack

- Java 21
- Maven 3.9+ (wrapper: `mvnw` / `mvnw.cmd`)
- Spring Boot 3.3.5
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-boot-starter-validation
  - spring-boot-starter-actuator
  - spring-boot-starter-test (test scope)
- PostgreSQL 16 (runtime)
- H2 (test scope)
- Flyway for database migrations
- springdoc-openapi 2.6.0 for Swagger/OpenAPI
- Lombok for boilerplate
- JUnit 5, Mockito, AssertJ, MockMvc for tests

## Project Structure

```
.
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── .github/workflows/ci.yml
└── src
    ├── main/
    │   ├── java/com/salian/productapi/
    │   │   ├── ProductApiApplication.java
    │   │   ├── config/          # OpenApiConfig, JpaAuditingConfig
    │   │   ├── domain/          # JPA entities (Product)
    │   │   ├── exception/       # DuplicateSkuException, ProductNotFoundException
    │   │   ├── repository/      # Spring Data JPA repositories
    │   │   ├── service/         # ProductService (business logic, transactions)
    │   │   └── web/             # ProductController, GlobalExceptionHandler, DTOs
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/    # Flyway SQL migrations (V<n>__description.sql)
    └── test/
        ├── java/com/salian/productapi/...
        └── resources/application-test.yml
```

## Build Instructions

Use the Maven wrapper provided:

```bash
# Compile and package
./mvnw -B clean package

# Full CI build: compile, run tests, package
./mvnw -B clean verify
```

On Windows, the equivalent batch file is `mvnw.cmd`.

Do not invent build commands that are not in the project. The CI workflow uses `mvn -B clean verify`.

## Test Instructions

```bash
# Run all tests
./mvnw -B clean verify

# Run a specific test class
./mvnw -B test -Dtest=ProductServiceTest
```

Test layers:

- **Service:** `@ExtendWith(MockitoExtension.class)` with mocked repositories and Logback appenders.
- **Web:** `@WebMvcTest` with MockMvc.
- **Persistence:** `@DataJpaTest` with in-memory H2 and `application-test.yml`.
- Tests use AssertJ (`assertThat`, `assertThatThrownBy`) and Mockito.

## Coding Conventions

Follow the existing conventions found in the code:

- **Language level:** Java 21. Use records for DTOs (e.g., `ProductRequest`, `ProductResponse`).
- **Entities:** JPA entities live in the `domain` package. Use Lombok `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` with `@Entity` and `@EntityListeners(AuditingEntityListener.class)`. Use `BigDecimal` for monetary values and `Instant` for audit timestamps. Use `@Version` for optimistic locking.
- **Services:** Marked with `@Service` and `@Transactional`. Class-level `@Transactional(readOnly = true)`; mutation methods override with `@Transactional`. Use SLF4J `LoggerFactory.getLogger(...)`. Throw domain exceptions (`DuplicateSkuException`, `ProductNotFoundException`) for business errors. Persistence writes should be flushed (e.g., `productRepository.flush()`) when the code must immediately catch database constraint violations.
- **Controllers:** `@RestController` with `@RequestMapping("/api/v1/products")`, `@RequiredArgsConstructor`, and springdoc `@Tag`/`@Operation` annotations. Use `Pageable` with `@PageableDefault` for paginated endpoints. Validate request bodies with `@Valid`.
- **Exception handling:** Global `@RestControllerAdvice` with `ProblemDetail` responses (RFC 7807). Return `ResponseEntity.created(...)` for POST and `ResponseEntity.noContent()` for DELETE.
- **Repositories:** Extend `JpaRepository`. Keep them interfaces; do not add custom implementations unless required.
- **Configuration:** Beans live in the `config` package. OpenAPI metadata is in `OpenApiConfig`; JPA auditing is in `JpaAuditingConfig`.
- **Logging:** Use SLF4J. Include relevant identifiers in log messages (id, sku, quantity, page number, etc.). Tests may attach a Logback `ListAppender` to assert log output.

## Database and Docker Considerations

- **Schema ownership:** Flyway owns the schema. Add migration files as `src/main/resources/db/migration/V<n>__description.sql`. Do not rely on `hibernate.hbm2ddl.auto` to modify the schema; `application.yml` sets `ddl-auto: validate`.
- **Entity/schema consistency:** JPA entity model must match the Flyway migrations. Any schema change requires both a new Flyway file and matching entity updates.
- **Local PostgreSQL:** `application.yml` defaults to `jdbc:postgresql://localhost:5432/productdb`. Override via environment variables:
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
  - `SERVER_PORT`
  - `LOG_LEVEL_APP`
- **Docker Compose:** `docker compose up --build` starts PostgreSQL and the API. The `api` service waits for `postgres` to be healthy before starting. The `Dockerfile` uses a non-root `appuser`, exposes port 8080, and runs a healthcheck against `/actuator/health`.
- **Database credentials:** Default credentials in `application.yml` and `docker-compose.yml` are for local development only. Always override for production/staging.

## Security Guidelines

- Do not hardcode secrets or credentials in source files.
- Do not commit `.env` files or personal access tokens.
- Database credentials are externalized via environment variables.
- The container runs as a non-root user (`appuser`).
- The OpenAPI docs and Actuator health endpoints are public by default in this project; review these before exposing to the internet.
- Lombok is marked `optional` and excluded from the packaged JAR.

## Validation Requirements Before Creating a Pull Request

1. Run the full CI command locally and confirm success:
   ```bash
   ./mvnw -B clean verify
   ```
2. Ensure the build produces no compile warnings or test failures.
3. Verify Flyway migration files are added for any schema change, and the JPA entities match the migrations.
4. Confirm `application.yml` and `docker-compose.yml` do not contain real secrets.
5. Do not create or merge pull requests without explicit user review/approval.
6. Do not push incomplete or failing branches.
