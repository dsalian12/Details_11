# Product API

Spring Boot 3 REST API built with Java 21 and Maven, backed by PostgreSQL via Spring Data JPA,
documented with OpenAPI/Swagger UI and packaged as a container image.

## Tech stack

| Concern | Choice |
| --- | --- |
| Language / build | Java 21, Maven |
| Framework | Spring Boot 3.3 (Web, Data JPA, Validation, Actuator) |
| Database | PostgreSQL 16, Flyway migrations |
| Boilerplate | Lombok |
| API docs | springdoc-openapi (Swagger UI) |
| Tests | JUnit 5, Mockito, AssertJ, Spring MockMvc, H2 |
| Packaging | Multi-stage Dockerfile, docker-compose |

## Project structure

```
.
├── Dockerfile                  # multi-stage build (Maven -> JRE 21)
├── docker-compose.yml          # API + PostgreSQL for local development
├── pom.xml
├── .github/workflows/ci.yml    # mvn clean verify on push/PR
└── src
    ├── main
    │   ├── java/com/salian/productapi
    │   │   ├── ProductApiApplication.java
    │   │   ├── config/         # OpenAPI + JPA auditing configuration
    │   │   ├── domain/         # JPA entities
    │   │   ├── exception/      # domain exceptions
    │   │   ├── repository/     # Spring Data repositories
    │   │   ├── service/        # business logic, transaction boundaries
    │   │   └── web/            # controllers, DTOs, error handling
    │   └── resources
    │       ├── application.yml
    │       └── db/migration/   # Flyway SQL migrations
    └── test
        ├── java/com/salian/productapi/...
        └── resources/application-test.yml
```

## Running locally

### With Docker Compose (recommended)

```bash
docker compose up --build
```

### With a local PostgreSQL

```bash
createdb productdb
./mvnw spring-boot:run
```

Configuration is environment-overridable: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
`SPRING_DATASOURCE_PASSWORD`, `SERVER_PORT`.

## API

Base path `/api/v1/products`.

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/v1/products` | Paginated list (`?page=0&size=20&sort=name,asc`) |
| GET | `/api/v1/products/{id}` | Fetch one product |
| POST | `/api/v1/products` | Create a product (returns `201` + `Location`) |
| PUT | `/api/v1/products/{id}` | Replace a product |
| DELETE | `/api/v1/products/{id}` | Delete a product (`204`) |

Errors are returned as RFC 7807 problem details: `400` validation failures, `404` unknown id,
`409` duplicate SKU.

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H 'Content-Type: application/json' \
  -d '{"sku":"SKU-1001","name":"Mechanical Keyboard","description":"87-key","price":129.99,"quantity":25}'
```

### Documentation and operations

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health: http://localhost:8080/actuator/health

## Tests

```bash
./mvnw clean verify
```

Covers the service layer (Mockito unit tests), the web layer (`@WebMvcTest` + MockMvc, including
validation and error mapping) and persistence (`@DataJpaTest` on in-memory H2).

## Database migrations

Schema is owned by Flyway (`src/main/resources/db/migration`); Hibernate runs with
`ddl-auto: validate` so the entity model and the migrated schema must agree. Add a new
`V<n>__description.sql` file for every schema change.

## Author

Dr. Dharmender Salian — Senior Software Engineer and Technical Lead (Java, Spring Boot, Microservices,
Python/FastAPI, AWS, Kafka, AI/ML and GenAI, RAG/LLM integrations).
[LinkedIn](https://www.linkedin.com/in/dr-dharmender-salian-2753341b5/)
