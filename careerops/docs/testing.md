# Testing Documentation — CareerOps

## 1. Testing Philosophy & Strategy

CareerOps adheres to a focused, high-signal testing approach:
- **Dedicated Test Profile (`test`)**: Tests run completely isolated from the production MySQL database using an H2 in-memory database (`jdbc:h2:mem:careerops_test`) with automatic schema creation and teardown (`ddl-auto: create-drop`).
- **Targeted Integration Tests**: Rather than generating hundreds of fragile mock tests, the test suite targets the core functional invariants:
  - Authentication success and failure modes
  - Opportunity CRUD lifecycle, pagination, and sorting
  - Application tracking workflow and duplicate prevention
  - Heuristic job-fit scoring mathematics and boundary checks
  - Input validation and exception translation

---

## 2. Automated Test Suite

The automated tests are located under `careerops/src/test/java/com/careerops/app`:

| Test Class | Scope | What It Verifies |
| :--- | :--- | :--- |
| `AuthIntegrationTest` | Spring Boot Integration (MockMvc) | - Registration creates candidate and returns JWT token<br/>- Login with valid credentials succeeds and returns JWT<br/>- Login with invalid password returns HTTP 400<br/>- Login for non-existent candidate fails |
| `OpportunityCrudTest` | Spring Boot Integration (MockMvc) | - Full CRUD lifecycle: Create, Read by ID, Update, Delete<br/>- Pagination (`page=0&size=5`) and custom sorting<br/>- Derived search filtering (`search=Senior&type=FULL_TIME`)<br/>- Read after delete returns HTTP 404 |
| `ApplicationCrudTest` | Spring Boot Integration (MockMvc) | - Application creation tracks opportunity for authenticated user<br/>- **Duplicate tracking prevention**: Submitting the same opportunity returns HTTP 409 Conflict<br/>- Status progression (`APPLIED` -> `INTERVIEW`)<br/>- Application removal and 404 verification |
| `JobFitServiceTest` | Service Unit / Integration | - Calculation produces match score between 0 and 100<br/>- Accurately separates matched skills from missing skills<br/>- Generates eligibility checks and actionable next steps<br/>- Ensures non-empty disclaimer notice |
| `ValidationExceptionTest` | Exception Handling & Validation | - Missing required fields on registration returns HTTP 400 with `fieldErrors`<br/>- Missing required fields on Opportunity creation returns HTTP 400 with specific field errors |

---

## 3. Running Automated Tests

Run the full test suite using Maven:

```bash
cd careerops
mvn test
```

### Test Configuration (`src/test/resources/application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:careerops_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password:
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect

app:
  jwt:
    secret: test-secret-key-that-is-long-enough-for-hmac-sha-256-algorithm
    expiration-minutes: 120
  fit:
    skill-weight: 40
    responsibility-weight: 25
    experience-weight: 20
    resume-weight: 15

ai:
  enabled: false
```

---

## 4. API Verification with Postman

In addition to automated Java tests, the repository includes a complete Postman collection and environment:
- Collection file: `careerops/postman/CareerOps.postman_collection.json`
- Environment file: `careerops/postman/CareerOps.postman_environment.json`

### Key Postman Features:
1. **Automated Token Chaining**: The Register and Login test scripts automatically extract `data.token` and store it in `pm.environment.set("token", ...)`. All subsequent requests automatically inherit Bearer authentication.
2. **Dynamic ID Chaining**: Creating a Skill, Opportunity, or Application automatically extracts the generated database `id` and assigns it to `{{skillId}}`, `{{opportunityId}}`, and `{{applicationId}}`.
3. **Assertion Verification**: Each request contains response code assertions and schema validations.
