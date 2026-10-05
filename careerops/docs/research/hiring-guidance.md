# Hiring Guidance & Job-Fit Research — CareerOps

## 1. Executive Summary

Modern applicant tracking systems (ATS) often fail students and early-career candidates by acting as opaque keyword scrapers. Candidates frequently face silent rejections without actionable feedback on why their profile was mismatched or how to improve their skills.

CareerOps bridges this gap by replacing black-box screening with **transparent, explainable heuristics**:
1. Skill overlap calculation with clear matched vs missing distinctions.
2. Semantic responsibility similarity using Cosine Vector overlap on candidate experience summaries.
3. Quantifiable resume quality scoring (presence of metrics, active verbs, and standard structural sections).
4. Explicit disclaimers clarifying that heuristic scoring assists candidate preparation rather than replacing human hiring judgment.

---

## 2. Multi-Factor Job-Fit Heuristic Weights

CareerOps weights candidate signals using four configurable dimensions totaling 100%:

```
Total Fit Score (100%) =
    40%  Skill Overlap
  + 25%  Responsibility / Description Similarity
  + 20%  Experience & Seniority Compatibility
  + 15%  Resume Structural Quality & Evidence
```

### 2.1 Skill Overlap (40%)
- Compares normalized candidate skill names against required opportunity skills.
- Categorizes skills into **Matched Skills** (green) and **Missing Skills** (amber/red).
- When a role requires 5 skills and a candidate has 4, this yields an 80% skill score contribution.

### 2.2 Responsibility & Cosine Similarity (25%)
- Evaluates the semantic overlap between candidate resume/experience text and the opportunity job description using term-frequency cosine vector comparison.
- Rewards concrete alignment with daily job duties rather than just bare keyword stuffing.

### 2.3 Experience & Seniority Check (20%)
- Extracts year thresholds (e.g., `2+ years experience`) using regex patterns.
- Labels status as `MATCH` (meets or exceeds), `PARTIAL` (within 1 year), or `BELOW` (significant gap).
- Recommends appropriate target opportunity levels (internships, junior roles) if below requirements.

### 2.4 Resume Structural Quality (15%)
- Offline inspection of text structure:
  - Standard section presence: `Experience`, `Education`, `Projects`, `Skills`.
  - Impact evidence: Frequency of quantifiable numbers and metrics (`%`, `$`, multiplier statistics).
  - Word count and density check (detects empty or unparseable scanned PDFs).

---

## 3. Ethical AI Principles & Disclaimer Standard

To ensure fairness, transparency, and safety:
- **No Hallucination**: AI models are strictly instructed via system prompt to never invent skills, certifications, or past employers not directly stated in the candidate's profile.
- **Explainability**: Every recommendation points directly to specific missing competencies or structural gaps.
- **Mandatory Disclaimer**: All fit calculations and AI reviews include an explicit disclaimer:
  > *"Fit is an explanatory heuristic based on the information available in your profile and the opportunity description. It is not a hiring decision or guarantee."*
