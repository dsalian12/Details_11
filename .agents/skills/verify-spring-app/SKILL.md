---
name: verify-spring-app
description: Validate the product-api Spring Boot application before a pull request
allowed-tools:
  - read
  - grep
  - glob
  - exec
---

Validate the `product-api` Spring Boot 3 application (Java 21, Maven) before a pull request. Follow this exact, repository-specific procedure. Do not invent commands or technologies.

## Repository facts

- **Language/build**: Java 21, Maven 3.9+, `mvnw` / `mvnw.cmd`
- **Framework**: Spring Boot 3.3.5, Spring Data JPA, Bean Validation, Actuator, springdoc-openapi
- **Tests**: JUnit 5, Mockito, AssertJ, Spring MockMvc, H2 in-memory for `@DataJpaTest`
- **Database migrations**: Flyway (`src/main/resources/db/migration/V<n>__description.sql`)
- **CI workflow**: `.github/workflows/ci.yml` runs `mvn -B clean verify`
- **Runtime options**:
  - `docker compose up --build` (recommended)
  - `createdb productdb` then `./mvnw spring-boot:run` (with local PostgreSQL)

## Step-by-step validation

### 1. Confirm the project layout

Verify these files/directories exist in the repository root:

- `pom.xml`
- `mvnw` (or `mvnw.cmd` on Windows)
- `src/main/java/com/salian/productapi/`
- `src/test/java/com/salian/productapi/`
- `src/test/resources/application-test.yml`
- `src/main/resources/application.yml`

If any are missing, stop and report the missing path.

### 2. Review the test setup

Read `src/test/resources/application-test.yml`. It must define:

- H2 in-memory datasource (`jdbc:h2:mem:...`)
- `jpa.hibernate.ddl-auto: create-drop`
- `flyway.enabled: false`

This means **PostgreSQL is not required to run the test suite**. All tests run against H2.

If this file has been changed to require a real database, warn the user before continuing.

### 3. Run the full Maven validation command

Execute the same command used by CI:

```bash
./mvnw -B clean verify
```

On Windows, if the shell is not MINGW/Git Bash, use:

```powershell
.\mvnw.cmd -B clean verify
```

Use a timeout of at least 5 minutes (300 seconds). If the first run must download dependencies, it may take longer.

### 4. If the build or tests fail

Stop immediately. Do not proceed to any later step, and do not create a commit or pull request. Report the following:

- The exact failing command.
- The Maven phase that failed (`test`, `compile`, `verify`, etc.).
- The failing test class and method names, if any.
- The first concrete error message and stack trace.
- The file paths involved.

If the failure is in a test you changed, ask the user how they want to proceed. Do not silently fix it unless explicitly instructed.

### 5. If the build succeeds

Confirm the output shows:

```
BUILD SUCCESS
```

Capture:

- Total tests run.
- Number of failures, errors, and skipped tests (all should be 0).
- Any warnings emitted (e.g., Spring Data `PageImpl` serialization warning).

### 6. Optional runtime smoke check

Only run this if the user explicitly asks for it, because it requires a running PostgreSQL or Docker.

- **Docker Compose**: `docker compose up --build`
  - Wait for `postgres` to be healthy and `api` to start on `http://localhost:8080`.
  - Check `/actuator/health`.
  - Stop with `docker compose down` when done.

- **Local PostgreSQL**:
  - `createdb productdb`
  - `./mvnw spring-boot:run`
  - Ensure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` are set or the defaults in `application.yml` are acceptable.

Do not run this step unless asked.

## Final report format

At the end, respond with a concise summary:

```
Validation status: [PASS / FAIL]
Build command: ./mvnw -B clean verify
Tests run: N
Failures: N
Errors: N
Skipped: N
Warnings: [list if any]
Ready for pull request: [YES / NO]
```

If `FAIL`, include the first failing test and error message. Do not create or merge a pull request after a failure.
