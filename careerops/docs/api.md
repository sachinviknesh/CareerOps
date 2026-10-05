# REST API Specification — CareerOps

Base URL: `http://localhost:8080` (or configured `${SERVER_PORT}`)
Authentication: Bearer JWT in the `Authorization` header (`Authorization: Bearer <token>`)

---

## 1. Authentication Endpoints (`/api/auth`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | None | Register a new candidate. Returns JWT and candidate info. |
| `POST` | `/api/auth/login` | None | Authenticate with email/password. Returns JWT. |

### Register Request Payload
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "password": "SecurePassword123"
}
```

### Auth Response (200 / 201)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "jane@example.com",
  "name": "Jane Doe"
}
```

---

## 2. Skill Catalog Endpoints (`/api/skills`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/skills` | Bearer | List all catalog skills. |
| `GET` | `/api/skills/{id}` | Bearer | Get skill by ID. |
| `POST` | `/api/skills` | Bearer | Create a new skill (unique name). |
| `PUT` | `/api/skills/{id}` | Bearer | Update skill name/category. |
| `DELETE` | `/api/skills/{id}` | Bearer | Remove skill from catalog. |

---

## 3. Opportunities Endpoints (`/api/opportunities`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/opportunities` | None | List opportunities with filter, pagination, & sorting. |
| `GET` | `/api/opportunities/closing-soon` | None | List opportunities closing in next N days (JPQL). |
| `GET` | `/api/opportunities/{id}` | None | Get opportunity details and required skills. |
| `POST` | `/api/opportunities` | Bearer | Create a new opportunity. |
| `PUT` | `/api/opportunities/{id}` | Bearer | Update opportunity details and required skills. |
| `DELETE` | `/api/opportunities/{id}` | Bearer | Delete opportunity. |
| `POST` | `/api/opportunities/{id}/ai-review` | Bearer | Run optional AI review against candidate profile. |
| `GET` | `/api/opportunities/{id}/fit` | Bearer | Calculate offline heuristic job-fit match score. |

### Query Parameters for `GET /api/opportunities`:
- `search` (String): Search within title, organization, or description.
- `type` (Enum): `FULL_TIME`, `INTERNSHIP`, `HACKATHON`, `CTF`.
- `roleFamily` (String): Role category filter.
- `skill` (String): Filter by associated skill name.
- `page` (int, default: 0): Zero-indexed page number.
- `size` (int, default: 10): Items per page.
- `sort` (String, default: `createdAt,desc`): Allowed sort fields: `id`, `title`, `deadline`, `postedAt`, `createdAt`, `organization`, `type`, `location`.

---

## 4. Candidate Profile Endpoints (`/api/candidates`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/candidates/me` | Bearer | Get authenticated candidate profile and skills. |
| `PUT` | `/api/candidates/me` | Bearer | Update candidate profile and associated skill IDs. |
| `DELETE` | `/api/candidates/me` | Bearer | Delete candidate account and all applications. |
| `POST` | `/api/candidates/me/resume` | Bearer | Upload PDF/DOCX resume file (`multipart/form-data`). |
| `GET` | `/api/candidates/me/resume-analysis` | Bearer | Run offline heuristic resume quality analysis. |

---

## 5. Application Tracking Endpoints (`/api/applications`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/applications` | Bearer | List tracked applications for candidate (paged/sorted). |
| `GET` | `/api/applications/stats` | Bearer | Aggregated count per stage for dashboard display. |
| `GET` | `/api/applications/{id}` | Bearer | Get specific application details by ID. |
| `POST` | `/api/applications` | Bearer | Track a new opportunity (prevents duplicates). |
| `PUT` | `/api/applications/{id}` | Bearer | Update stage, next action date, or notes. |
| `DELETE` | `/api/applications/{id}` | Bearer | Remove opportunity from tracker. |

### Application Create Request Payload
```json
{
  "opportunityId": 1,
  "status": "APPLIED",
  "appliedAt": "2026-10-05",
  "nextActionDate": "2026-10-15",
  "notes": "Applied via referral link on company career portal."
}
```

---

## 6. Standard Error Responses

All API errors return consistent JSON error bodies:

### Validation Error (HTTP 400)
```json
{
  "timestamp": "2026-10-05T16:00:00",
  "status": 400,
  "error": "Validation Failed",
  "fieldErrors": {
    "title": "Title is required",
    "type": "Type is required"
  },
  "path": "/api/opportunities"
}
```

### Duplicate Entry (HTTP 409)
```json
{
  "timestamp": "2026-10-05T16:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "You have already tracked this opportunity.",
  "path": "/api/applications"
}
```
