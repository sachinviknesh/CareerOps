package com.careerops.app.entity;

public enum OpportunityType {
    JOB,
    INTERNSHIP,
    HACKATHON,
    CTF_COMPETITION,
    CTF, // Maintained for backward compatibility
    FELLOWSHIP,
    CODING_COMPETITION,
    QUIZ,
    INNOVATION_CHALLENGE,
    RESEARCH_COMPETITION,
    STUDENT_COMPETITION,
    SCHOLARSHIP,
    OTHER;

    public String getDisplayName() {
        return switch (this) {
            case JOB -> "Job";
            case INTERNSHIP -> "Internship";
            case HACKATHON -> "Hackathon";
            case CTF_COMPETITION, CTF -> "CTF Competition";
            case FELLOWSHIP -> "Fellowship";
            case CODING_COMPETITION -> "Coding Competition";
            case QUIZ -> "Quiz";
            case INNOVATION_CHALLENGE -> "Innovation Challenge";
            case RESEARCH_COMPETITION -> "Research Competition";
            case STUDENT_COMPETITION -> "Student Competition";
            case SCHOLARSHIP -> "Scholarship";
            case OTHER -> "Other Opportunity";
        };
    }

    public String getActionLabel() {
        return switch (this) {
            case HACKATHON, CTF_COMPETITION, CTF, CODING_COMPETITION, QUIZ, INNOVATION_CHALLENGE, STUDENT_COMPETITION -> "Register Now";
            default -> "Apply Now";
        };
    }
}
