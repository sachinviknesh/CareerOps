package com.careerops.app.service;

import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.Opportunity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class EligibilityService {

    public static class EligibilityResult {
        private String status; // ELIGIBLE, POSSIBLY_ELIGIBLE, NOT_ELIGIBLE, UNKNOWN
        private String statusLabel; // ✅ Eligible, ⚠️ Possibly Eligible, etc.
        private List<String> passedCriteria = new ArrayList<>();
        private List<String> missingCriteria = new ArrayList<>();
        private String summary;

        public EligibilityResult() {}

        public EligibilityResult(String status, String statusLabel, String summary) {
            this.status = status;
            this.statusLabel = statusLabel;
            this.summary = summary;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getStatusLabel() { return statusLabel; }
        public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
        public List<String> getPassedCriteria() { return passedCriteria; }
        public void setPassedCriteria(List<String> passedCriteria) { this.passedCriteria = passedCriteria; }
        public List<String> getMissingCriteria() { return missingCriteria; }
        public void setMissingCriteria(List<String> missingCriteria) { this.missingCriteria = missingCriteria; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
    }

    public EligibilityResult evaluate(Opportunity opportunity, Candidate candidate) {
        if (candidate == null) {
            return new EligibilityResult("UNKNOWN", "❓ Eligibility Unknown", "Log in to check your personalized eligibility.");
        }

        String eligText = opportunity.getEligibility();
        String eduReq = opportunity.getEducationRequirement();
        Integer expReq = opportunity.getExperienceYearsRequirement();

        // If no criteria specified at all, return UNKNOWN / Open to all
        if ((eligText == null || eligText.isBlank()) &&
                (eduReq == null || eduReq.isBlank()) &&
                expReq == null) {
            EligibilityResult res = new EligibilityResult("ELIGIBLE", "✅ Eligible", "Open opportunity with no restrictive constraints specified.");
            res.getPassedCriteria().add("Open to all registered candidates and students");
            return res;
        }

        List<String> passed = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        String normalizedElig = (eligText != null ? eligText : "").toLowerCase(Locale.ROOT);
        String normalizedEdu = (eduReq != null ? eduReq : "").toLowerCase(Locale.ROOT);

        // 1. Education / Degree Check
        if (candidate.getDegree() != null && !candidate.getDegree().isBlank()) {
            String candidateDegree = candidate.getDegree().toLowerCase(Locale.ROOT);
            if (normalizedEdu.contains("b.tech") || normalizedEdu.contains("b.e") || normalizedEdu.contains("engineering") ||
                    normalizedElig.contains("engineering") || normalizedElig.contains("b.tech") || normalizedElig.contains("cs / it")) {
                if (candidateDegree.contains("b.tech") || candidateDegree.contains("b.e") || candidateDegree.contains("computer") || candidateDegree.contains("engineering") || candidateDegree.contains("cs")) {
                    passed.add("Degree criteria matched (" + candidate.getDegree() + ")");
                }
            } else if (!normalizedEdu.isBlank()) {
                passed.add("Degree: " + candidate.getDegree() + " considered for " + eduReq);
            }
        }

        // 2. Graduation Year / Student Status Check
        if (candidate.getGraduationYear() != null) {
            int gradYear = candidate.getGraduationYear();
            int currentYear = LocalDate.now().getYear();

            if (normalizedElig.contains("student") || normalizedElig.contains("enrolled") || normalizedElig.contains("college student")) {
                if (gradYear >= currentYear) {
                    passed.add("Current student status verified (Batch of " + gradYear + ")");
                } else {
                    failed.add("Event restricted to currently enrolled college students (Graduation: " + gradYear + ")");
                }
            }

            // Check explicit batch years
            if (normalizedElig.contains("2024") || normalizedElig.contains("2025") || normalizedElig.contains("2026") || normalizedElig.contains("2027") || normalizedElig.contains("2028")) {
                if (normalizedElig.contains(String.valueOf(gradYear))) {
                    passed.add("Graduation batch of " + gradYear + " is explicitly eligible");
                }
            }
        }

        // 3. Experience check
        if (expReq != null && expReq > 0) {
            int candidateExp = candidate.getExperience() != null ? candidate.getExperience() : 0;
            if (candidateExp >= expReq) {
                passed.add("Experience requirement satisfied (" + candidateExp + " yrs >= " + expReq + " yrs required)");
            } else {
                failed.add("Requires " + expReq + " yrs experience (Candidate has " + candidateExp + " yrs)");
            }
        }

        // Formulate decision
        EligibilityResult result = new EligibilityResult();
        result.setPassedCriteria(passed);
        result.setMissingCriteria(failed);

        if (!failed.isEmpty()) {
            if (passed.size() > 0) {
                result.setStatus("POSSIBLY_ELIGIBLE");
                result.setStatusLabel("⚠️ Possibly Eligible");
                result.setSummary("Meets some qualifications, but some criteria like batch year or experience may require verification.");
            } else {
                result.setStatus("NOT_ELIGIBLE");
                result.setStatusLabel("❌ Not Eligible");
                result.setSummary("Does not meet the mandatory criteria specified for this opportunity.");
            }
        } else if (!passed.isEmpty()) {
            result.setStatus("ELIGIBLE");
            result.setStatusLabel("✅ Eligible");
            result.setSummary("Candidate profile satisfies the stated eligibility criteria.");
        } else {
            result.setStatus("POSSIBLY_ELIGIBLE");
            result.setStatusLabel("⚠️ Possibly Eligible");
            result.setSummary("Eligibility requirements are broad. Please verify specific college/event guidelines.");
        }

        return result;
    }
}
