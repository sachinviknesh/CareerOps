# CareerOps REST API Documentation

## Base URL
```
http://localhost:8080
```

## Authentication Header
Protected endpoints require an `Authorization` header containing the JWT token received from login or registration:
```http
Authorization: Bearer <your-jwt-token>
```

## External Services & AI API Key Configuration
External service integrations (AI Grounded Fit Review & Remote Job Feed ingestion) authenticate using the configured API Key:
```env
API_KEY=YOUR_GCP_API_KEY
```
- **AI Analysis Provider (`AI_API_KEY`)**: Configured for generative analysis & grounded fit scoring via Gemini (`https://generativelanguage.googleapis.com/v1beta/openai/`).
- **Remote Job Feed Provider (`JOB_API_KEY`)**: Configured for external opportunity search and synchronization feeds.

---

## 1. System Health & Status

### `GET /`
- **Description**: Verifies service status and application health.
- **Authentication**: Public
- **Response** (`200 OK`):
```json
{
  "status": "UP",
  "application": "CareerOps"
}
```

---

## 2. Authentication APIs

### `POST /api/auth/register`
- **Description**: Registers a new user/candidate account with BCrypt password encryption and generates a JWT.
- **Authentication**: Public
- **Request Body**:
```json
{
  "name": "Sachin Kumar",
  "email": "sachin@example.com",
  "password": "Password@123"
}
```
- **Response** (`201 CREATED`):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresInSeconds": 86400
}
```
- **Error Responses**:
  - `400 BAD REQUEST`: Validation failure (invalid email format or short password).
  - `409 CONFLICT`: Email address already registered.

### `POST /api/auth/login`
- **Description**: Authenticates user credentials and returns a valid JWT.
- **Authentication**: Public
- **Request Body**:
```json
{
  "email": "sachin@example.com",
  "password": "Password@123"
}
```
- **Response** (`200 OK`):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresInSeconds": 86400
}
```
- **Error Responses**:
  - `401 UNAUTHORIZED`: Invalid email or password.

---

## 3. Opportunity APIs

### `GET /api/opportunities`
- **Description**: Retrieves a paginated and sorted list of opportunities with multi-parameter filtering.
- **Authentication**: Public
- **Query Parameters**:
  - `search` (string): Keyword matching title, organization, or description.
  - `type` (enum): `JOB`, `INTERNSHIP`, `HACKATHON`, `CTF_COMPETITION`, `FELLOWSHIP`, `CODING_COMPETITION`, etc.
  - `roleFamily` (string): e.g. `Cybersecurity`, `Data & AI`, `Software Engineering`.
  - `page` (int, default `0`): Zero-indexed page number.
  - `size` (int, default `10`): Items per page.
  - `sort` (string, default `createdAt,desc`): e.g. `deadline,asc`, `title,asc`.
- **Response** (`200 OK`):
```json
{
  "content": [
    {
      "id": 1,
      "title": "SOC Analyst (Tier 1)",
      "organization": "CyberDefense Labs India",
      "type": "JOB",
      "location": "Chennai, Tamil Nadu",
      "workMode": "OFFLINE",
      "deadline": "2026-10-30",
      "url": "https://cyberdefenselabs.in/careers/soc-101",
      "active": true,
      "requiredSkills": [
        {"id": 1, "name": "SIEM", "category": "Security"},
        {"id": 2, "name": "Linux", "category": "Systems"}
      ],
      "city": "Chennai",
      "state": "Tamil Nadu",
      "country": "India",
      "travelBadge": "🔴 Offline — Travel required",
      "actionLabel": "Apply Now"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 20,
  "totalPages": 2,
  "first": true,
  "last": false
}
```

### `GET /api/opportunities/{id}`
- **Description**: Retrieves full details and required skills for an opportunity.
- **Authentication**: Public
- **Response** (`200 OK`) / `404 NOT FOUND`.

### `POST /api/opportunities`
- **Description**: Creates a new opportunity record.
- **Authentication**: Bearer Token required
- **Request Body**:
```json
{
  "title": "Cybersecurity CTF 2026",
  "organization": "ABC Engineering College",
  "type": "CTF_COMPETITION",
  "roleFamily": "Cybersecurity",
  "description": "State-level cybersecurity competition covering packet analysis, web exploitation, and cryptography.",
  "location": "Chennai, Tamil Nadu",
  "workMode": "OFFLINE",
  "url": "https://abcengg.edu.in/ctf",
  "deadline": "2026-10-25",
  "requiredSkillIds": [1, 2]
}
```
- **Response** (`201 CREATED`).

### `PUT /api/opportunities/{id}`
- **Description**: Updates an existing opportunity.
- **Authentication**: Bearer Token required
- **Response** (`200 OK`) / `404 NOT FOUND`.

### `DELETE /api/opportunities/{id}`
- **Description**: Removes an opportunity.
- **Authentication**: Bearer Token required
- **Response** (`204 NO CONTENT`) / `404 NOT FOUND`.

### `GET /api/opportunities/search?title={title}`
- **Description**: Derived JPA query to search opportunities by title keyword.
- **Authentication**: Public
- **Response** (`200 OK`).

### `GET /api/opportunities/type/{type}`
- **Description**: JPQL query retrieving active opportunities by type (`JOB`, `HACKATHON`, `CTF_COMPETITION`, etc.).
- **Authentication**: Public
- **Response** (`200 OK`).

### `GET /api/opportunities/location/{location}`
- **Description**: Derived JPA query retrieving opportunities matching location keyword.
- **Authentication**: Public
- **Response** (`200 OK`).

### `GET /api/opportunities/organization/{organization}`
- **Description**: Derived JPA query retrieving opportunities matching organization name.
- **Authentication**: Public
- **Response** (`200 OK`).

### `GET /api/opportunities/{id}/fit`
- **Description**: Evaluates authenticated candidate against opportunity using the 4-part Fit Engine heuristic (Skills, Experience, Resume Quality, Description Similarity).
- **Authentication**: Bearer Token required
- **Response** (`200 OK`):
```json
{
  "score": 92,
  "confidence": "HIGH",
  "matchedSkills": ["Linux", "Network Security", "Python"],
  "missingSkills": ["Digital Forensics"],
  "evidenceFound": ["Linux — found in resume"],
  "nextActions": [
    "Practice web exploitation and cryptography on CTFtime",
    "Tailor your resume to emphasize matched security projects"
  ],
  "disclaimer": "Fit is an explanatory heuristic based on profile information and description."
}
```

---

## 4. Application Tracker APIs

### `GET /api/applications`
- **Description**: Paged list of tracked applications for the authenticated user/candidate.
- **Authentication**: Bearer Token required
- **Query Parameters**: `status`, `type`, `page`, `size`, `sort`.
- **Response** (`200 OK`).

### `GET /api/applications/stats`
- **Description**: Aggregated count of candidate applications by stage.
- **Authentication**: Bearer Token required
- **Response** (`200 OK`):
```json
{
  "total": 5,
  "toApply": 2,
  "applied": 1,
  "inProgress": 1,
  "interview": 1,
  "selected": 0,
  "rejected": 0
}
```

### `POST /api/applications`
- **Description**: Adds an opportunity to the candidate's tracker (prevents duplicate applications).
- **Authentication**: Bearer Token required
- **Request Body**:
```json
{
  "opportunityId": 1,
  "status": "APPLIED",
  "appliedAt": "2026-10-05",
  "notes": "Submitted application through portal."
}
```
- **Response** (`201 CREATED`).
- **Error Responses**:
  - `409 CONFLICT`: Opportunity already tracked by this candidate.

### `PUT /api/applications/{id}`
- **Description**: Updates application stage (`INTERVIEW`, `SELECTED`, `REJECTED`, etc.), next action date, or notes.
- **Authentication**: Bearer Token required
- **Response** (`200 OK`).

### `DELETE /api/applications/{id}`
- **Description**: Removes an application from the tracker.
- **Authentication**: Bearer Token required
- **Response** (`204 NO CONTENT`).

---

## 5. Skill Catalog APIs

### `GET /api/skills`
- **Description**: Returns all technical and domain skills alphabetically.
- **Authentication**: Bearer Token required
- **Response** (`200 OK`):
```json
[
  {"id": 1, "name": "Algorithms", "category": "Computer Science"},
  {"id": 2, "name": "Cybersecurity", "category": "Security"},
  {"id": 3, "name": "Java", "category": "Backend"},
  {"id": 4, "name": "Python", "category": "Data & AI"}
]
```

### `POST /api/skills`
- **Description**: Creates a new skill with duplicate prevention.
- **Authentication**: Bearer Token required
- **Request Body**:
```json
{
  "name": "Kubernetes",
  "category": "Cloud & DevOps"
}
```
- **Response** (`201 CREATED`).

### `PUT /api/skills/{id}`
- **Description**: Updates skill name or category.
- **Authentication**: Bearer Token required
- **Response** (`200 OK`).

### `DELETE /api/skills/{id}`
- **Description**: Deletes a skill.
- **Authentication**: Bearer Token required
- **Response** (`204 NO CONTENT`).

---

## 6. User Management APIs

### `GET /api/users`
- **Description**: Lists registered user accounts (passwords excluded).
- **Authentication**: Bearer Token required
- **Response** (`200 OK`).

### `GET /api/users/{id}`
- **Description**: Retrieves user account by ID.
- **Authentication**: Bearer Token required
- **Response** (`200 OK`).

### `GET /api/users/me`
- **Description**: Retrieves currently authenticated user profile.
- **Authentication**: Bearer Token required
- **Response** (`200 OK`).
