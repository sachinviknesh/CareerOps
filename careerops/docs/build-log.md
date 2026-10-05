# Build & Implementation Log — CareerOps

## Timeline of Phases Completed

### Phase 1: Spring Boot Initialization & Foundation
- Initialized Spring Boot 3.3.4 project with Java 17, Spring Web, Spring Data JPA, Spring Security, Validation, and MySQL driver.
- Configured YAML configurations (`application.yml`) supporting environment variable externalization (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, etc.).
- Configured H2 in-memory profile in `src/test/resources/application.yml` for isolated zero-side-effect test execution.

### Phase 2: JPA Entity Design
- Created exactly 4 primary JPA entities:
  - `Candidate`: Auth credentials, profile fields, resume text, linked skills, and applications.
  - `Opportunity`: Role titles, organizations, opportunity types (`FULL_TIME`, `INTERNSHIP`, `HACKATHON`, `CTF`), deadlines, work modes, and required skills.
  - `Application`: Pipeline workflow (`TO_APPLY`, `APPLIED`, `ASSESSMENT`, `INTERVIEW`, `OFFER`, `REJECTED`, `WITHDRAWN`), notes, and unique constraint (`uk_candidate_opportunity`).
  - `Skill`: Standardized competency catalog with unique name constraints.

### Phase 3: Repositories & Query Layer
- Implemented `CandidateRepository`, `OpportunityRepository`, `SkillRepository`, and `ApplicationRepository`.
- Built derived queries: `findByEmailIgnoreCase`, `findByType`, `findByTitleContainingIgnoreCase`, `findByCandidateIdAndStatus`, `findByNameIgnoreCase`.
- Built JPQL queries: `findClosingSoon(days)` querying active opportunities with deadlines in the next N days.

### Phase 4 & 5: Service & REST Controller Layer
- Implemented concrete services: `AuthService`, `CandidateService`, `OpportunityService`, `ApplicationService`, `SkillService`.
- Created REST controllers with OpenAPI annotations:
  - `AuthController` (`/api/auth/register`, `/api/auth/login`)
  - `CandidateController` (`/api/candidates/me`, `/api/candidates/me/resume`)
  - `OpportunityController` (`/api/opportunities`, `/api/opportunities/closing-soon`, `/api/opportunities/{id}/fit`, `/api/opportunities/{id}/ai-review`)
  - `ApplicationController` (`/api/applications`, `/api/applications/stats`)
  - `SkillController` (`/api/skills`)
  - `JobFitController` (`/api/opportunities/{id}/fit`, `/api/candidates/me/resume-analysis`)

### Phase 6 & 7: Security, Validation & Global Exception Handling
- Integrated stateless JWT authentication filter (`JwtAuthenticationFilter`) with HMAC-SHA256 signing and expiration validation.
- Configured BCrypt password hashing.
- Added comprehensive global exception handler (`GlobalExceptionHandler`) returning structured JSON for `MethodArgumentNotValidException`, `ResourceNotFoundException`, `BadRequestException`, `DataIntegrityViolationException`, and `AuthenticationException`.

### Phase 8 & 9: Pagination, Sorting & Search
- Implemented safe pageable creation with whitelist sort validation (`id`, `title`, `deadline`, `postedAt`, `createdAt`, `organization`, `type`, `location`).
- Added multi-criteria filtering for title keyword, opportunity type, and required skill.

### Phase 10: OpenAPI / Swagger UI
- Integrated `springdoc-openapi-starter-webmvc-ui` (v2.6.0).
- Configured JWT Bearer security scheme in Swagger UI for interactive browser-based testing.

### Phase 11, 12 & 13: Resume Extraction & Offline Job-Fit Heuristics
- Integrated Apache Tika (`tika-core` & `tika-parsers-standard-package`) for parsing PDF and DOCX uploads into clean plain text.
- Implemented offline heuristic analysis:
  - Section presence detection (`experience`, `education`, `skills`, `projects`).
  - Action verb frequency and quantifiable metric counting.
- Built multi-factor job-fit engine weighting Skill Overlap (40%), Responsibilities Cosine Similarity (25%), Experience Comparison (20%), and Resume Quality (15%).

### Phase 14: React Modern Frontend (`careerops-ui`)
- Developed single-page React application with Vite.
- Implemented full feature suite:
  - Interactive authentication (Register / Login) with token persistence.
  - Opportunity discovery cards with live search, type filters, and pagination.
  - Interactive Application Pipeline Kanban / Table tracker with stage updating and note editing.
  - Resume upload dropzone with real-time offline heuristic evaluation.
  - Deep job-fit breakdown displaying matched vs missing skills, confidence badge, and actionable next steps.
- Built and validated with `npm run build` (production build compiled successfully).

### Phase 15: Optional Grounded AI Review
- Implemented `AiAnalysisService` using standard OpenAI-compatible completions API.
- Strict system prompt constraining output to verified facts and structured JSON format.
- Graceful offline fallback when disabled or unavailable.

### Phase 16: Postman Suite
- Created `careerops/postman/CareerOps.postman_environment.json`.
- Created `careerops/postman/CareerOps.postman_collection.json` covering 18 endpoints with automated test assertions and dynamic token/id chaining.

### Phase 17: Automated Tests
- Implemented JUnit 5 & MockMvc test suite with H2 in-memory test profile:
  - `AuthIntegrationTest`: Authentication success and failure modes.
  - `OpportunityCrudTest`: Full CRUD lifecycle, pagination, and sorting.
  - `ApplicationCrudTest`: Tracking pipeline and duplicate opportunity prevention.
  - `JobFitServiceTest`: Explainable heuristic match score and skill breakdown.
  - `ValidationExceptionTest`: Request validation failure handling.

### Phase 18: Documentation & Delivery
- Authored `architecture.md`, `entity-design.md`, `api.md`, `testing.md`, `hiring-guidance.md`, and master `README.md`.
