
# CareerOps – Faculty Demonstration Guide (Step-by-Step)

This guide documents the exact 29-step sequence for demonstrating the CareerOps REST API mini-project to college faculty and external evaluators.

---

## Prerequisites & Architecture Check

- **Java**: Version 17+
- **Spring Boot**: 3.3.4
- **Database**: MySQL Server (Database: `careerops`) with automatic H2 file fallback if offline
- **Swagger UI**: Accessible at `http://localhost:8080/swagger-ui.html`
- **Frontend App**: Accessible at `http://localhost:5174/`

---

## 29-Step Faculty Demonstration Sequence

### STEP 1: Start MySQL
Ensure MySQL is running on port 3306 (or the system will automatically utilize the embedded MySQL-mode data file at `./data/careerops`):
```bash
# Verify MySQL connection or start service
mysql -u root -p
CREATE DATABASE IF NOT EXISTS careerops;
```

### STEP 2: Start Spring Boot
Launch the backend from the `careerops/` folder:
```bash
./mvnw spring-boot:run
```
*Note the startup log displaying:*
`Started CareerOpsApplication in ... seconds`
`DataInitializer completed successfully. Total opportunities: 22, skills: 15, applications: 2`

### STEP 3: Open Swagger UI
Open your browser to:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
Show the faculty the OpenAPI 3.0 documentation, grouped tags (`Auth`, `Opportunities`, `Applications`, `Skills`, `Users`), schemas, and the "Authorize" button.

### STEP 4: Register a User
- In Swagger, expand **`POST /api/auth/register`**.
- Click **Try it out**.
- Enter JSON:
```json
{
  "name": "Faculty Evaluator",
  "email": "faculty@college.edu",
  "password": "password123"
}
```
- Click **Execute**. Show **HTTP 201 Created** with user payload and no exposed password.

### STEP 5: Login
- Expand **`POST /api/auth/login`**.
- Click **Try it out**.
- Enter JSON:
```json
{
  "email": "faculty@college.edu",
  "password": "password123"
}
```
- Click **Execute**. Show **HTTP 200 OK** returning JWT token.

### STEP 6: Copy JWT
- Copy the JWT token value from the `token` field in the response JSON.

### STEP 7: Use Swagger Authorize
- Scroll to the top of Swagger UI and click the green **Authorize** button.
- In the `bearerAuth` input box, enter the JWT token (or `Bearer <token>`).
- Click **Authorize**, then **Close**. Swagger is now authenticated.

### STEP 8: Create an Opportunity (CRUD - Create)
- Expand **`POST /api/opportunities`**.
- Click **Try it out**.
- Enter JSON:
```json
{
  "title": "Graduate Software Engineer 2026",
  "organization": "Google India",
  "type": "JOB",
  "roleFamily": "Software Engineering",
  "description": "Develop high-scale backend services in Java and Spring Boot.",
  "location": "Bangalore, Karnataka",
  "workMode": "HYBRID",
  "deadline": "2026-12-31"
}
```
- Click **Execute**. Show **HTTP 201 Created** with generated `id`. Note this `id` (e.g., 23).

### STEP 9: GET All Opportunities (CRUD - Read)
- Expand **`GET /api/opportunities`**.
- Click **Try it out**, leave parameters default, and click **Execute**.
- Show **HTTP 200 OK** returning a paginated list with total elements, page, size, and content.

### STEP 10: GET Opportunity by ID
- Expand **`GET /api/opportunities/{id}`**.
- Enter the `id` created in Step 8 (e.g., `23`).
- Click **Execute**. Show **HTTP 200 OK** with matching details.

### STEP 11: UPDATE Opportunity (CRUD - Update)
- Expand **`PUT /api/opportunities/{id}`**.
- Enter the `id` (e.g., `23`).
- Enter updated JSON:
```json
{
  "title": "Senior Graduate Software Engineer 2026",
  "organization": "Google India",
  "type": "JOB",
  "roleFamily": "Software Engineering",
  "description": "Updated job description with advanced distributed systems requirements.",
  "location": "Bangalore, Karnataka",
  "workMode": "REMOTE",
  "deadline": "2026-12-31"
}
```
- Click **Execute**. Show **HTTP 200 OK** with modified `title` and `workMode: REMOTE`.

### STEP 12: DELETE Opportunity (CRUD - Delete)
- Expand **`DELETE /api/opportunities/{id}`**.
- Enter the `id` (e.g., `23`).
- Click **Execute**. Show **HTTP 204 No Content**.
- Then execute **`GET /api/opportunities/{id}`** with the same ID to demonstrate **404 Not Found**.

### STEP 13: Search Opportunity (Derived Query)
- Expand **`GET /api/opportunities/search`**.
- Enter `title`: `Cybersecurity`.
- Click **Execute**. Show **HTTP 200 OK** returning matched records using derived method `findByTitleContainingIgnoreCase`.

### STEP 14: Filter by Opportunity Type (JPQL Query)
- Expand **`GET /api/opportunities/type/{type}`**.
- Enter `type`: `HACKATHON`.
- Click **Execute**. Show **HTTP 200 OK** demonstrating JPQL query `findActiveByType`.

### STEP 15: Filter by Location (Derived Query)
- Expand **`GET /api/opportunities/location/{location}`**.
- Enter `location`: `Chennai`.
- Click **Execute**. Show **HTTP 200 OK** returning opportunities situated in Chennai.

### STEP 16: Demonstrate Derived JPA Query
Highlight in IntelliJ IDEA code:
`List<Opportunity> findByLocationContainingIgnoreCase(String location);`
in `OpportunityRepository.java` and execute **`GET /api/opportunities/location/{location}`**.

### STEP 17: Demonstrate JPQL Query
Highlight in IntelliJ IDEA code:
```java
@Query("""
    select o from Opportunity o
    where o.type = :type
      and o.active = true
    order by o.deadline asc nulls last
""")
List<Opportunity> findActiveByType(@Param("type") OpportunityType type);
```
in `OpportunityRepository.java` and execute **`GET /api/opportunities/type/{type}`**.

### STEP 18: Demonstrate Pagination
- Expand **`GET /api/opportunities`**.
- Enter `page`: `0`, `size`: `5`.
- Click **Execute**. Show metadata:
  - `page: 0`
  - `size: 5`
  - `totalElements: 22`
  - `totalPages: 5`

### STEP 19: Demonstrate Sorting
- Expand **`GET /api/opportunities`**.
- Set `sort`: `deadline,asc`.
- Click **Execute**. Show items ordered chronologically by upcoming deadline.
- Change `sort`: `title,desc` and execute to show reverse alphabetical ordering.

### STEP 20: Create Application (JPA Relationship)
- Expand **`POST /api/applications`**.
- Enter JSON:
```json
{
  "opportunityId": 1,
  "status": "APPLIED",
  "notes": "Submitted through campus portal with resume."
}
```
- Click **Execute**. Show **HTTP 201 Created** returning the new application record with linked opportunity and candidate/user IDs.

### STEP 21: Show User → Application → Opportunity Relationship
- Expand **`GET /api/applications/user/{userId}`** (JPQL query).
- Enter `userId`: `1`.
- Click **Execute**. Show the application object containing both foreign references:
  - `user_id`
  - `opportunity_id` with embedded full opportunity metadata.

### STEP 22: Update Application
- Expand **`PUT /api/applications/{id}`**.
- Enter application ID from Step 20.
- Enter JSON:
```json
{
  "opportunityId": 1,
  "status": "INTERVIEW",
  "notes": "Passed technical test. Scheduled for interview round 1."
}
```
- Click **Execute**. Show status transition to `INTERVIEW`.

### STEP 23: Delete Application
- Expand **`DELETE /api/applications/{id}`**.
- Enter application ID.
- Click **Execute**. Show **HTTP 204 No Content**.

### STEP 24: Demonstrate Bean Validation (HTTP 400)
- Expand **`POST /api/opportunities`**.
- Clear the body and enter invalid JSON (missing required `title` and `type`):
```json
{
  "organization": "Invalid Test Org"
}
```
- Click **Execute**. Show **HTTP 400 Bad Request** with clear field validation errors:
```json
{
  "status": 400,
  "error": "Validation Error",
  "fieldErrors": {
    "title": "Title is required",
    "type": "Type is required"
  }
}
```

### STEP 25: Demonstrate Exception Handling (HTTP 404)
- Expand **`GET /api/opportunities/{id}`**.
- Enter nonexistent `id`: `999999`.
- Click **Execute**. Show structured **HTTP 404 Not Found** response from `GlobalExceptionHandler`:
```json
{
  "timestamp": "...",
  "status": 404,
  "error": "Not Found",
  "message": "Opportunity not found with id: 999999",
  "path": "/api/opportunities/999999"
}
```

### STEP 26: Demonstrate JWT Security (HTTP 403 / 401)
- Click **Authorize** button in Swagger, click **Logout**, then close modal.
- Try calling **`POST /api/applications`** or **`GET /api/applications`** without token.
- Show **HTTP 403 Forbidden** / **401 Unauthorized**, proving endpoints are secured.

### STEP 27: Show Swagger / OpenAPI Spec
- Point faculty to `http://localhost:8080/api-docs` to show the full OpenAPI 3.0 JSON specification.

### STEP 28: Show MySQL Database Tables
Open MySQL Workbench, IntelliJ Database tool, or terminal:
```sql
USE careerops;
SHOW TABLES;
-- Shows: users, opportunity, application, skill, candidate, etc.

DESCRIBE application;
-- Shows foreign keys: user_id, opportunity_id, candidate_id

SELECT id, name, email, role FROM users;
SELECT id, title, organization, type, deadline FROM opportunity LIMIT 5;
SELECT id, status, user_id, opportunity_id FROM application;
```

### STEP 29: Show Application Logs
Show terminal / console logs displaying structured SLF4J / Logback messages:
- `INFO ... AuthService : Login success for candidate: ...`
- `INFO ... OpportunityService : Opportunity created: ...`
- `WARN ... GlobalExceptionHandler : Validation failed at ...`
- Confirm that passwords, JWT keys, and database secrets are never exposed in log output.

---

## Final Viva Key Answers

1. **Why Layered Architecture?**
   - Separation of concerns: Controllers handle HTTP requests & validation, Services encapsulate business rules, Repositories handle data access via JPA, Entities model the database tables.
2. **What is the Difference Between Derived Queries and JPQL?**
   - *Derived Queries*: Spring Data parses method names (e.g. `findByLocationContainingIgnoreCase`) and generates SQL automatically.
   - *JPQL*: Developer writes object-oriented HQL queries targeting entity classes rather than raw database tables (e.g. `SELECT o FROM Opportunity o WHERE o.type = :type`).
3. **How does JWT Authentication Work?**
   - Stateless authentication: The client exchanges credentials for a signed token. Every subsequent request includes `Authorization: Bearer <token>`, verified by `JwtAuthenticationFilter` without server-side session state.
