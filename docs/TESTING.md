# CareerOps Test Suite & Quality Assurance Documentation

## 1. Overview
CareerOps features an automated test suite verifying data integrity, business logic correctness, security enforcement, validation boundaries, and exception handling across all entities.

The test suite leverages:
- **JUnit 5 (Jupiter)** for test runner and lifecycle hooks.
- **AssertJ & Hamcrest** for fluent, readable assertions.
- **Spring Boot Test (`@SpringBootTest`)** for end-to-end slice and integration testing.
- **Spring Security Test** for JWT and role-based authorization verification.
- **Spring MockMvc** for controller request/response validation.
- **In-Memory H2 / Test Database Profile (`@ActiveProfiles("test")`)** with transactional rollback (`@Transactional`) ensuring zero side effects.

---

## 2. Test Execution Commands

### Run All Backend Tests
```bash
cd careerops
.\mvnw.cmd test
```

### Run Specific Test Classes
```bash
# Run Auth Integration Tests
.\mvnw.cmd test -Dtest=AuthIntegrationTest

# Run Opportunity CRUD and Pagination Tests
.\mvnw.cmd test -Dtest=OpportunityCrudTest

# Run Application Lifecycle Tests
.\mvnw.cmd test -Dtest=ApplicationCrudTest

# Run Bean Validation and Exception Handling Tests
.\mvnw.cmd test -Dtest=ValidationExceptionTest

# Run Platform, Travel Intel & Discovery Tests
.\mvnw.cmd test -Dtest=OpportunityPlatformTest
```

---

## 3. Test Suites & Coverage Breakdown

### 3.1 `AuthIntegrationTest.java`
| Test Case | Scenario | Expected Outcome |
| :--- | :--- | :--- |
| `testUserRegistrationSuccess` | Register new user with valid name, email, and password | Returns `200 OK` with JWT token and user details; password hashed with BCrypt |
| `testDuplicateEmailRegistration` | Register with an already existing email address | Returns `409 Conflict` with clear error message |
| `testUserLoginSuccess` | Login with correct email and password | Returns `200 OK` with valid JWT token |
| `testUserLoginInvalidPassword` | Login with incorrect password | Returns `401 Unauthorized` |
| `testUnauthenticatedAccessToProtectedApi` | Access `/api/applications` without `Authorization` header | Returns `401 Unauthorized` |

### 3.2 `OpportunityCrudTest.java`
| Test Case | Scenario | Expected Outcome |
| :--- | :--- | :--- |
| `testCreateOpportunity` | `POST /api/opportunities` with valid title, type, location | Returns `201 Created` with persisted opportunity |
| `testGetOpportunityById` | `GET /api/opportunities/{id}` for existing opportunity | Returns `200 OK` with JSON entity |
| `testUpdateOpportunity` | `PUT /api/opportunities/{id}` modifying location and status | Returns `200 OK` with updated fields |
| `testDeleteOpportunity` | `DELETE /api/opportunities/{id}` followed by GET | Returns `204 No Content`; subsequent GET returns `404 Not Found` |
| `testPaginationAndSorting` | `GET /api/opportunities?page=0&size=5&sort=deadline,asc` | Returns `Page<Opportunity>` containing up to 5 items sorted by deadline |
| `testDerivedQueryFilterByType` | `GET /api/opportunities/type/HACKATHON` | Returns only opportunities of type `HACKATHON` |
| `testDerivedQueryFilterByLocation` | `GET /api/opportunities/location/Chennai` | Returns opportunities matching "Chennai" |
| `testJpqlQueryClosingSoon` | `GET /api/opportunities/closing-soon?days=7` | Returns active opportunities with deadline within 7 days |

### 3.3 `ApplicationCrudTest.java`
| Test Case | Scenario | Expected Outcome |
| :--- | :--- | :--- |
| `testApplyToOpportunity` | `POST /api/applications` with valid user and opportunity ID | Returns `201 Created` with initial status `APPLIED` |
| `testDuplicateApplicationBlocked` | Apply to the same opportunity twice | Returns `409 Conflict` (Duplicate application prevented) |
| `testUpdateApplicationStatus` | `PATCH /api/applications/{id}/status` from `APPLIED` to `INTERVIEW` | Returns `200 OK` with updated status |
| `testDeleteApplication` | `DELETE /api/applications/{id}` | Returns `204 No Content` |
| `testApplicationStats` | `GET /api/applications/stats` | Returns status count breakdown (`applied`, `interview`, `selected`, `rejected`) |
| `testJpqlFindApplicationsByUserId` | Query applications by authenticated user ID | Returns list of user's tracked applications |

### 3.4 `ValidationExceptionTest.java`
| Test Case | Scenario | Expected Outcome |
| :--- | :--- | :--- |
| `testBlankTitleValidation` | `POST /api/opportunities` with blank title `""` | Returns `400 Bad Request` with field error `"title: Title is required"` |
| `testInvalidEmailValidation` | `POST /api/auth/register` with `"not-an-email"` | Returns `400 Bad Request` with field error `"email: Invalid email format"` |
| `testShortPasswordValidation` | `POST /api/auth/register` with password `"123"` | Returns `400 Bad Request` with size validation error |
| `testResourceNotFound` | `GET /api/opportunities/999999` | Handled by `GlobalExceptionHandler` returning `404 Not Found` JSON |
| `testMethodNotAllowed` | Send `PATCH` to an endpoint that only supports `GET` | Handled returning `405 Method Not Allowed` |

### 3.5 `OpportunityPlatformTest.java`
| Test Case | Scenario | Expected Outcome |
| :--- | :--- | :--- |
| `testOnlineTravelIntel` | Evaluate travel for `workMode=ONLINE` | `travelRequired=false`, `locationFitScore=100`, badge `🟢 Online` |
| `testOfflineChennaiToBangalore` | Evaluate travel from Chennai to Bangalore venue | `travelRequired=true`, distance calculated ~290-330 km, badge `🔴 Offline` |
| `testEligibilityEngine` | Evaluate candidate batch 2026 for B.Tech requirement | Status evaluates to `ELIGIBLE` with breakdown |
| `testOpportunityTypes` | Verify action labels across all 12 types | Hackathons show "Register Now", Jobs show "Apply Now" |
| `testNotificationPreferenceFiltering` | Candidate sets min fit score 85, incoming match is 60 | Notification is filtered out |

---

## 4. End-to-End Test Automation (Browser)
CareerOps also includes browser-level automated verification scripts executed via Headless Chromium / DevTools:
- **`e2e-browser-test.cjs`**: Verifies login form, dashboard navigation, Opportunity Discovery feed, filter controls, and application tracking tabs.
- **`test-fit-browser.cjs`**: Verifies candidate profile fit scoring and location badge rendering on live opportunities.
