package com.careerops.app.service.jobsource;

import com.careerops.app.dto.match.ExternalJobDto;
import com.careerops.app.dto.match.JobSearchCriteria;
import com.careerops.app.entity.Opportunity;
import com.careerops.app.entity.OpportunityType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MockJobSourceProvider implements JobSourceProvider {

    public static final String SOURCE_CODE = "CAREEROPS_INDIA_HUB";
    public static final String SOURCE_NAME = "CareerOps India Opportunities Feed";

    @Value("${jobs.sources.mock.enabled:true}")
    private boolean enabled = true;

    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    @Override
    public String getSourceType() {
        return "EVENT_PLATFORM";
    }

    @Override
    public String getBaseUrl() {
        return "https://careerops.platform/india";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<ExternalJobDto> fetchJobs() {
        return getCuratedOpportunities();
    }

    @Override
    public List<ExternalJobDto> searchJobs(JobSearchCriteria criteria) {
        List<ExternalJobDto> all = getCuratedOpportunities();
        if (criteria == null) {
            return all;
        }

        return all.stream()
                .filter(job -> {
                    if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
                        String kw = criteria.keyword().toLowerCase().trim();
                        boolean matchTitle = job.title().toLowerCase().contains(kw);
                        boolean matchCompany = job.company().toLowerCase().contains(kw);
                        boolean matchDesc = job.description().toLowerCase().contains(kw);
                        boolean matchSkills = job.requiredSkills() != null &&
                                job.requiredSkills().stream().anyMatch(s -> s.toLowerCase().contains(kw));
                        if (!matchTitle && !matchCompany && !matchDesc && !matchSkills) return false;
                    }

                    if (criteria.location() != null && !criteria.location().isBlank()) {
                        String loc = criteria.location().toLowerCase().trim();
                        if (job.location() == null || !job.location().toLowerCase().contains(loc)) {
                            return false;
                        }
                    }

                    if (criteria.workMode() != null && !criteria.workMode().isBlank()) {
                        if (job.workMode() == null || !job.workMode().equalsIgnoreCase(criteria.workMode().trim())) {
                            return false;
                        }
                    }

                    if (criteria.jobType() != null && !criteria.jobType().isBlank()) {
                        String jt = criteria.jobType().toLowerCase().trim();
                        boolean matchJt = job.jobType().toLowerCase().contains(jt);
                        if (!matchJt) return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Opportunity normalizeJob(ExternalJobDto externalJob) {
        Opportunity opp = new Opportunity();
        opp.setTitle(externalJob.title().trim());
        opp.setOrganization(externalJob.company() != null ? externalJob.company().trim() : "Unknown");

        OpportunityType type;
        try {
            type = OpportunityType.valueOf(externalJob.jobType().toUpperCase().trim());
        } catch (Exception e) {
            type = OpportunityType.JOB;
        }
        opp.setType(type);
        opp.setOpportunityType(type);

        opp.setDescription(externalJob.description());
        opp.setLocation(externalJob.location());
        opp.setWorkMode(externalJob.workMode());
        opp.setUrl(externalJob.url());
        opp.setPostedAt(externalJob.postedDate() != null ? externalJob.postedDate() : LocalDate.now());
        opp.setDeadline(externalJob.deadline() != null ? externalJob.deadline() : LocalDate.now().plusDays(30));
        opp.setActive(true);
        opp.setSource(SOURCE_CODE);
        opp.setExternalJobId(externalJob.externalId());
        opp.setExternalId(externalJob.externalId());
        opp.setNormalizedTitle(externalJob.title().trim().toLowerCase());

        opp.setCity(externalJob.city());
        opp.setState(externalJob.state());
        opp.setCountry(externalJob.country() != null ? externalJob.country() : "India");
        opp.setVenue(externalJob.venue());
        opp.setEligibility(externalJob.eligibility());
        opp.setPreferredSkills(externalJob.preferredSkills());
        opp.setExperienceRequirement(externalJob.experienceRequirement());
        opp.setEducationRequirement(externalJob.educationRequirement());
        opp.setRegistrationDeadline(externalJob.registrationDeadline() != null ? externalJob.registrationDeadline() : opp.getDeadline());
        opp.setEventStartDate(externalJob.eventStartDate());
        opp.setEventEndDate(externalJob.eventEndDate());
        opp.setStatus("OPEN");

        String titleLower = externalJob.title().toLowerCase();
        if (titleLower.contains("cyber") || titleLower.contains("security") || titleLower.contains("soc") || titleLower.contains("ctf")) {
            opp.setRoleFamily("Cybersecurity");
        } else if (titleLower.contains("data") || titleLower.contains("analyst") || titleLower.contains("ml") || titleLower.contains("machine learning")) {
            opp.setRoleFamily("Data & AI");
        } else {
            opp.setRoleFamily("Software Engineering");
        }

        return opp;
    }

    private List<ExternalJobDto> getCuratedOpportunities() {
        List<ExternalJobDto> list = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 1. Jobs - SOC Analyst (Chennai)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "JOB-IND-101",
                "SOC Analyst (Tier 1)", "CyberDefense Labs India",
                "Chennai, Tamil Nadu", "OFFLINE", "JOB",
                "Monitor security alerts in real-time SIEM platforms, triage incidents, analyze malware artifacts, and escalate verified cyber threats.",
                "https://cyberdefenselabs.in/careers/soc-101",
                today.minusDays(2), today.plusDays(25),
                List.of("SIEM", "Incident Response", "Network Security", "Linux"),
                "Chennai", "Tamil Nadu", "India", "CyberDefense Tech Park, OMR, Chennai",
                "B.Tech / B.E in CS / IT / Cyber Security or equivalent. Batches 2023-2026 eligible.",
                "Wireshark, Splunk, Python scripting", 1, "B.Tech in Computer Science / IT / Cyber Security",
                today.plusDays(25), null, null
        ));

        // 2. Jobs - Security Analyst (Bangalore)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "JOB-IND-102",
                "Security Analyst", "ThreatMatrix India",
                "Bangalore, Karnataka", "HYBRID", "JOB",
                "Perform vulnerability assessments, review cloud security postures, configure firewall policies, and assist with ISO 27001 audit controls.",
                "https://threatmatrix.in/careers/sec-analyst",
                today.minusDays(5), today.plusDays(18),
                List.of("Vulnerability Assessment", "Cybersecurity", "Network Security", "Python"),
                "Bangalore", "Karnataka", "India", "ThreatMatrix Campus, Electronic City, Bangalore",
                "Engineering graduates with understanding of OWASP Top 10 and network security fundamentals.",
                "Burp Suite, Nessus, AWS Security", 2, "B.Tech / B.E in CS / ECE / IT",
                today.plusDays(18), null, null
        ));

        // 3. Jobs - Cybersecurity Engineer (Hyderabad)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "JOB-IND-103",
                "Cybersecurity Engineer", "CyberShield Technologies",
                "Hyderabad, Telangana", "OFFLINE", "JOB",
                "Architect security controls, automate intrusion detection with Python, implement zero-trust network boundaries, and conduct adversary simulation exercises.",
                "https://cybershield.in/jobs/eng-103",
                today.minusDays(3), today.plusDays(30),
                List.of("Cybersecurity", "Linux", "Python", "Docker", "Network Security"),
                "Hyderabad", "Telangana", "India", "HITEC City, Madhapur, Hyderabad",
                "B.Tech / M.Tech in CS or Information Security. 2+ years of hands-on experience required.",
                "Kubernetes security, SIEM automation, CI/CD security scanning", 2, "Bachelor's or Master's in Computer Science",
                today.plusDays(30), null, null
        ));

        // 4. Internships - Cybersecurity Intern (Chennai)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "INT-IND-201",
                "Cybersecurity Intern", "SecureCloud Networks",
                "Chennai, Tamil Nadu", "HYBRID", "INTERNSHIP",
                "Hands-on internship analyzing packet captures, configuring intrusion detection rules, updating threat intelligence feeds, and participating in blue-team incident response simulations.",
                "https://securecloud.in/internships/sec-ops",
                today.minusDays(1), today.plusDays(40),
                List.of("Linux", "Network Security", "Python", "Wireshark"),
                "Chennai", "Tamil Nadu", "India", "Tidel Park, Taramani, Chennai",
                "Open to all enrolled engineering students (Batches 2025, 2026, 2027, 2028). Stipend: ₹25,000/month.",
                "Basic understanding of TCP/IP, Linux command line, and Nmap", 0, "Enrolled in B.Tech / B.E / B.Sc Computer Science",
                today.plusDays(35), today.plusDays(45), today.plusDays(135)
        ));

        // 5. Internships - SOC Intern (Bangalore)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "INT-IND-202",
                "SOC Operations Intern", "Kavach Security",
                "Bangalore, Karnataka", "OFFLINE", "INTERNSHIP",
                "Work closely with senior SOC analysts to analyze phishing reports, investigate SIEM alerts, write detection signatures, and generate security metrics reports.",
                "https://kavachsec.in/interns/soc",
                today.minusDays(4), today.plusDays(15),
                List.of("SIEM", "Cybersecurity", "Incident Response", "Linux"),
                "Bangalore", "Karnataka", "India", "Kavach SOC Operations Hub, Whitefield, Bangalore",
                "Pre-final and final year college students passionate about security operations. ₹30,000/month.",
                "Familiarity with Wireshark and Log Analysis", 0, "B.Tech in Computer Science / Information Technology",
                today.plusDays(15), today.plusDays(25), today.plusDays(115)
        ));

        // 6. Internships - Software Security Intern (Pune)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "INT-IND-203",
                "Software Security Intern", "Apex Secure Systems",
                "Pune, Maharashtra", "HYBRID", "INTERNSHIP",
                "Review code repositories for security flaws, integrate SAST and DAST scanners into CI/CD pipelines, and assist in fixing OWASP Top 10 vulnerabilities.",
                "https://apexsec.in/careers/intern-pune",
                today.minusDays(2), today.plusDays(22),
                List.of("Java", "Python", "Web Security", "Git"),
                "Pune", "Maharashtra", "India", "Hinjawadi Phase 1, Pune",
                "Students pursuing Computer Science or related degree. Batches 2025-2027.",
                "Knowledge of secure coding principles and REST APIs", 0, "B.Tech / MCA",
                today.plusDays(20), today.plusDays(30), today.plusDays(120)
        ));

        // 7. Hackathons - AI Hackathon (Online)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "HACK-IND-301",
                "NextGen India AI Hackathon 2026", "TechBuilders Global",
                "Online", "ONLINE", "HACKATHON",
                "48-hour national virtual hackathon building intelligent agentic workflows, LLM applications, and real-time data pipelines. Cash prizes worth ₹5,00,000 and direct interview opportunities.",
                "https://hackathon.techbuilders.in/2026",
                today.minusDays(3), today.plusDays(10),
                List.of("Python", "React", "Docker", "REST APIs", "Machine Learning"),
                "Online", "Pan-India", "India", "Virtual Platform (Discord & Devpost)",
                "Open to all college students, developers, and researchers across India. Teams of 2 to 4 members.",
                "FastAPI, Next.js, LangChain or OpenAI/Gemini SDK experience", 0, "No degree barrier — open to all enrolled students",
                today.plusDays(8), today.plusDays(12), today.plusDays(14)
        ));

        // 8. Hackathons - Cybersecurity Hackathon (IIT Madras, Chennai)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "HACK-IND-302",
                "Shaastra CyberHack 2026", "IIT Madras",
                "Chennai, Tamil Nadu", "OFFLINE", "HACKATHON",
                "Build state-of-the-art defensive cyber tooling, automated incident response agents, or decentralized threat intelligence feeds during this 36-hour in-person hackathon on the IIT Madras campus.",
                "https://shaastra.org/cyberhack-2026",
                today.minusDays(1), today.plusDays(12),
                List.of("Cybersecurity", "Python", "Linux", "Network Security", "Docker"),
                "Chennai", "Tamil Nadu", "India", "Central Lecture Theatre, IIT Madras, Chennai",
                "Open to all enrolled undergraduate and postgraduate college students. Valid college ID required. Free food and on-campus stay provided.",
                "Socket programming, threat modeling, rapid prototyping", 0, "Full-time college students (B.Tech / M.Tech / MCA)",
                today.plusDays(10), today.plusDays(20), today.plusDays(22)
        ));

        // 9. Hackathons - Innovation Hackathon (Bangalore)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "HACK-IND-303",
                "Bengaluru Tech Summit Hackathon", "Karnataka Innovation Authority",
                "Bangalore, Karnataka", "OFFLINE", "HACKATHON",
                "Grand hackathon tackling civic tech, smart mobility, healthcare intelligence, and cyber resilience. Supported by top tech unicorns with ₹10,00,000 total prize pool.",
                "https://bts-hackathon.karnataka.gov.in",
                today.minusDays(5), today.plusDays(16),
                List.of("React", "Python", "Java", "Docker", "SQL"),
                "Bangalore", "Karnataka", "India", "Bangalore Palace Grounds, Bengaluru",
                "Open to student teams and early-stage startup innovators across India.",
                "Cloud infrastructure, mobile app development", 0, "Students and working professionals",
                today.plusDays(14), today.plusDays(28), today.plusDays(30)
        ));

        // 10. CTF - Web Security CTF (Online)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "CTF-IND-401",
                "National Web Security CTF 2026", "Null Community India",
                "Online", "ONLINE", "CTF_COMPETITION",
                "Jeopardy-style capture the flag tournament testing OWASP Top 10 vulnerabilities, server-side template injection, OAuth flows, and zero-day web challenge exploitation.",
                "https://nullcon.in/ctf-2026",
                today.minusDays(2), today.plusDays(7),
                List.of("Web Security", "Python", "Linux", "Burp Suite"),
                "Online", "Pan-India", "India", "CTFd Platform & Discord Server",
                "Open to all individual cybersecurity enthusiasts, ethical hackers, and college teams across India.",
                "SQL injection, XSS, SSRF, JWT exploitation", 0, "Open to all students and self-taught security researchers",
                today.plusDays(6), today.plusDays(8), today.plusDays(9)
        ));

        // 11. CTF - Network Security CTF (ABC College, Chennai)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "CTF-IND-402",
                "Cybersecurity CTF 2026", "ABC Engineering College",
                "Chennai, Tamil Nadu", "OFFLINE", "CTF_COMPETITION",
                "Prestigious state-level intercollegiate cybersecurity competition featuring forensics, packet analysis, network intrusion, and binary exploitation flags.",
                "https://abcengg.edu.in/symposium/ctf-2026",
                today.minusDays(1), today.plusDays(15),
                List.of("Cybersecurity", "Networking", "Linux", "Nmap", "Wireshark"),
                "Chennai", "Tamil Nadu", "India", "Computer Science Auditorium, ABC Engineering College, Chennai",
                "Enrolled college students from recognized universities/institutions across India. Teams of 2 to 3 members.",
                "Wireshark packet inspection, Linux command line, Nmap scanning, basic forensics", 0, "B.E / B.Tech / BCA / MCA / B.Sc CS students",
                today.plusDays(12), today.plusDays(20), today.plusDays(20)
        ));

        // 12. CTF - Capture The Flag National Championship (Delhi)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "CTF-IND-403",
                "National Capture The Flag Championship", "National Cyber Coordination Centre",
                "Delhi", "OFFLINE", "CTF_COMPETITION",
                "Elite collegiate CTF competition sponsored by government cyber agencies. Winners receive cash rewards and fast-track interviews for national cyber defense laboratories.",
                "https://nccc.gov.in/ctf-championship",
                today.minusDays(4), today.plusDays(21),
                List.of("Reverse Engineering", "Cryptography", "Web Security", "Linux", "Cybersecurity"),
                "Delhi", "Delhi NCR", "India", "Vigyan Bhawan, New Delhi",
                "Indian college students with valid university enrollment proof. Batches 2024-2028.",
                "Ghidra/IDA Pro, Cryptography solvers, Memory forensics (Volatility)", 0, "Undergraduate / Postgraduate Indian citizens",
                today.plusDays(18), today.plusDays(35), today.plusDays(36)
        ));

        // 13. Fellowships - Cybersecurity Fellowship (Bangalore)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "FEL-IND-501",
                "National Cybersecurity Research Fellowship", "C-DAC Bangalore",
                "Bangalore, Karnataka", "HYBRID", "FELLOWSHIP",
                "6-month paid research fellowship focusing on critical infrastructure defense, OT/ICS security, and quantum-resistant cryptographic algorithms.",
                "https://cdac.in/fellowships/cyber-2026",
                today.minusDays(6), today.plusDays(24),
                List.of("Cybersecurity", "Linux", "Python", "Cryptography"),
                "Bangalore", "Karnataka", "India", "C-DAC Innovation Hub, Electronic City, Bangalore",
                "Graduating seniors or recent graduates with strong foundation in cybersecurity and computer systems. ₹45,000/month stipend.",
                "Prior publications, open-source security tool contributions, or CTF achievements", 0, "B.Tech / M.Tech in CS / Information Security / ECE",
                today.plusDays(20), today.plusDays(45), today.plusDays(225)
        ));

        // 14. Fellowships - Technology Fellowship (Delhi / Hybrid)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "FEL-IND-502",
                "Civic Technology & Public Policy Fellowship", "Digital India Foundation",
                "Delhi", "HYBRID", "FELLOWSHIP",
                "Join a 9-month fellowship building open-source public digital goods, data privacy tooling, and citizen-first applications for rural governance.",
                "https://digitalindiafoundation.org/fellowship",
                today.minusDays(8), today.plusDays(19),
                List.of("Python", "React", "SQL", "REST APIs"),
                "Delhi", "Delhi NCR", "India", "India Habitat Centre, Lodhi Road, New Delhi",
                "Open to graduates and final year students committed to public-interest technology. Monthly stipend: ₹50,000.",
                "Full-stack web engineering, API design, user research", 0, "Any Bachelor's or Master's degree",
                today.plusDays(16), today.plusDays(40), today.plusDays(310)
        ));

        // 15. Competitions - National Coding Competition (Online)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "COMP-IND-601",
                "CodeSprint India 2026", "GeeksCoding India",
                "Online", "ONLINE", "CODING_COMPETITION",
                "High-speed algorithmic problem solving contest with 5 dynamic programming and graph challenges. ₹2,00,000 in cash prizes and Amazon gift vouchers.",
                "https://codesprint.in/contest-2026",
                today.minusDays(1), today.plusDays(5),
                List.of("Algorithms", "Data Structures", "Java", "Python"),
                "Online", "Pan-India", "India", "Virtual Code Execution Arena",
                "Open to all students enrolled in schools or colleges in India.",
                "Competitive programming experience, time complexity optimization", 0, "Open to all students",
                today.plusDays(4), today.plusDays(6), today.plusDays(6)
        ));

        // 16. Competitions - Innovation Challenge (Mumbai)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "COMP-IND-602",
                "Smart India Innovation Challenge", "Ministry of Innovation & MSME",
                "Mumbai, Maharashtra", "OFFLINE", "INNOVATION_CHALLENGE",
                "National competition seeking breakthrough hardware and software solutions for industrial automation, cybersecurity resilience, and clean energy.",
                "https://innovation.gov.in/challenge-2026",
                today.minusDays(7), today.plusDays(26),
                List.of("Python", "React", "Docker", "Machine Learning"),
                "Mumbai", "Maharashtra", "India", "Bombay Exhibition Centre, Nesco, Goregaon, Mumbai",
                "Open to student teams (3 to 6 members) across recognized technical institutes. Seed grants up to ₹15,00,000.",
                "Working prototype, business plan, technical documentation", 0, "Recognized degree in Engineering, Design, or Science",
                today.plusDays(24), today.plusDays(45), today.plusDays(47)
        ));

        // 17. Competitions - Student Research Competition (Chennai)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "COMP-IND-603",
                "National Student Research Symposium", "Anna University",
                "Chennai, Tamil Nadu", "OFFLINE", "RESEARCH_COMPETITION",
                "Present original research papers on artificial intelligence, network defense, cryptography, and distributed systems before distinguished IEEE fellows and faculty.",
                "https://annauniv.edu/symposium/research-2026",
                today.minusDays(3), today.plusDays(17),
                List.of("Cybersecurity", "Machine Learning", "Python", "Data Structures"),
                "Chennai", "Tamil Nadu", "India", "Tagore Engineering Hall, Anna University Guindy Campus, Chennai",
                "Undergraduate and postgraduate students with original unpublished research papers.",
                "Technical writing, LaTeX formatting, oral presentation skills", 0, "B.E / B.Tech / M.E / M.Tech / M.S / Ph.D students",
                today.plusDays(14), today.plusDays(28), today.plusDays(29)
        ));

        // 18. Scholarship - National STEM Excellence Scholarship (Online / Delhi)
        list.add(new ExternalJobDto(
                SOURCE_CODE, "SCH-IND-701",
                "National STEM & Cyber Excellence Scholarship", "Foundation for Technology Leaders",
                "Online", "ONLINE", "SCHOLARSHIP",
                "Merit-cum-means scholarship providing ₹1,00,000 financial support and 1-on-1 industry mentorship for engineering students specializing in Cyber Security and AI.",
                "https://techleadersfoundation.org/scholarship-2026",
                today.minusDays(4), today.plusDays(35),
                List.of("Cybersecurity", "Python", "Algorithms"),
                "Online", "Pan-India", "India", "Online Portal & Virtual Interview",
                "Students currently in 2nd, 3rd, or 4th year of B.Tech / B.E with minimum 7.5 CGPA.",
                "Academic transcripts, statement of purpose, faculty recommendation", 0, "Enrolled in B.Tech / B.E (CS/IT/ECE)",
                today.plusDays(30), null, null
        ));

        return list;
    }
}
