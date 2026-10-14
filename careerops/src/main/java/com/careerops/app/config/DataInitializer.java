package com.careerops.app.config;

import com.careerops.app.entity.*;
import com.careerops.app.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final SkillRepository skillRepo;
    private final OpportunityRepository opportunityRepo;
    private final UserRepository userRepo;
    private final CandidateRepository candidateRepo;
    private final ApplicationRepository applicationRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SkillRepository skillRepo,
                           OpportunityRepository opportunityRepo,
                           UserRepository userRepo,
                           CandidateRepository candidateRepo,
                           ApplicationRepository applicationRepo,
                           PasswordEncoder passwordEncoder) {
        this.skillRepo = skillRepo;
        this.opportunityRepo = opportunityRepo;
        this.userRepo = userRepo;
        this.candidateRepo = candidateRepo;
        this.applicationRepo = applicationRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Starting DataInitializer check for CareerOps sample data...");
        seedSkills();
        seedOpportunities();
        seedDemoUserAndApplications();
        log.info("DataInitializer completed successfully. Total opportunities: {}, skills: {}, applications: {}",
                opportunityRepo.count(), skillRepo.count(), applicationRepo.count());
    }

    private void seedSkills() {
        if (skillRepo.count() > 0) return;

        List<Skill> skills = List.of(
                new Skill("Java", "Programming Languages"),
                new Skill("Spring Boot", "Backend Frameworks"),
                new Skill("MySQL", "Databases"),
                new Skill("React", "Frontend Frameworks"),
                new Skill("TypeScript", "Frontend Languages"),
                new Skill("Python", "Programming Languages"),
                new Skill("Docker", "DevOps & Cloud"),
                new Skill("AWS", "DevOps & Cloud"),
                new Skill("Kubernetes", "DevOps & Cloud"),
                new Skill("REST APIs", "Backend Engineering"),
                new Skill("Cybersecurity", "Security"),
                new Skill("Linux", "Systems & OS"),
                new Skill("Git", "Tools & Version Control"),
                new Skill("Microservices", "Architecture"),
                new Skill("Machine Learning", "Artificial Intelligence")
        );
        skillRepo.saveAll(skills);
        log.info("Seeded {} core technical skills", skills.size());
    }

    private void seedOpportunities() {
        if (opportunityRepo.count() >= 20) return;

        Set<Skill> javaSkills = new HashSet<>(skillRepo.findAll());

        List<Opportunity> oppList = List.of(
                createOpp("Junior Java Backend Developer", "Infosys Technologies",
                        OpportunityType.JOB, "Backend Engineering",
                        "Develop scalable microservices using Spring Boot, Spring Data JPA, and MySQL.",
                        "Chennai", "Tamil Nadu", "HYBRID", LocalDate.now().plusDays(30), "https://careers.infosys.com"),

                createOpp("Cybersecurity Analyst Intern", "Wipro CyberDefense",
                        OpportunityType.INTERNSHIP, "Security Operations",
                        "Participate in SOC operations, vulnerability assessments, and penetration testing.",
                        "Bangalore", "Karnataka", "ON_SITE", LocalDate.now().plusDays(20), "https://careers.wipro.com"),

                createOpp("Smart India Hackathon 2026", "Ministry of Education Innovation Cell",
                        OpportunityType.HACKATHON, "Competitive Innovation",
                        "Premier national hackathon solving real-world government and industry problem statements.",
                        "Delhi", "Delhi", "HYBRID", LocalDate.now().plusDays(45), "https://sih.gov.in"),

                createOpp("National Cyber Defense CTF Challenge", "Data Security Council of India (DSCI)",
                        OpportunityType.CTF_COMPETITION, "Offensive Security",
                        "Capture The Flag competition spanning cryptography, web exploitation, and binary forensics.",
                        "Chennai", "Tamil Nadu", "REMOTE", LocalDate.now().plusDays(15), "https://dsci.in/events/ctf"),

                createOpp("Full Stack Engineering Fellowship", "Turing Academy India",
                        OpportunityType.FELLOWSHIP, "Software Engineering",
                        "Intensive 12-week fellowship covering high-scale distributed systems and React architecture.",
                        "Bangalore", "Karnataka", "REMOTE", LocalDate.now().plusDays(60), "https://turing.com/fellowship"),

                createOpp("Google Summer of Code Fellowship", "Google Open Source",
                        OpportunityType.FELLOWSHIP, "Open Source Development",
                        "Contribute to open source foundations under mentorship from global software engineers.",
                        "Bangalore", "Karnataka", "REMOTE", LocalDate.now().plusDays(90), "https://summerofcode.withgoogle.com"),

                createOpp("AI / ML Research Internship", "IIT Madras Research Park",
                        OpportunityType.RESEARCH_COMPETITION, "Research & AI",
                        "Research novel transformer compression architectures and vision-language models.",
                        "Chennai", "Tamil Nadu", "ON_SITE", LocalDate.now().plusDays(25), "https://respark.iitm.ac.in"),

                createOpp("FinTech Innovation Challenge 2026", "Razorpay Payments",
                        OpportunityType.INNOVATION_CHALLENGE, "Financial Technology",
                        "Build groundbreaking payments infrastructure prototypes and next-gen UPI systems.",
                        "Bangalore", "Karnataka", "HYBRID", LocalDate.now().plusDays(18), "https://razorpay.com/challenges"),

                createOpp("National Merit STEM Scholarship", "Tata Trusts Foundation",
                        OpportunityType.SCHOLARSHIP, "Higher Education Grant",
                        "Full financial scholarship for undergraduate engineering students excelling in computer science.",
                        "Mumbai", "Maharashtra", "REMOTE", LocalDate.now().plusDays(40), "https://tatatrusts.org/scholarships"),

                createOpp("Cloud Infrastructure Intern", "Amazon Web Services (AWS)",
                        OpportunityType.INTERNSHIP, "Cloud Architecture",
                        "Work on automated container deployments, IAM governance, and resilient VPC infrastructure.",
                        "Hyderabad", "Telangana", "HYBRID", LocalDate.now().plusDays(35), "https://amazon.jobs"),

                createOpp("Data Engineering Associate", "Accenture Solutions",
                        OpportunityType.JOB, "Data Platforms",
                        "Design and optimize high-throughput ETL pipelines with Apache Spark and SQL databases.",
                        "Coimbatore", "Tamil Nadu", "ON_SITE", LocalDate.now().plusDays(28), "https://accenture.com/in-en/careers"),

                createOpp("React Frontend Developer", "Zoho Corporation",
                        OpportunityType.JOB, "Frontend Engineering",
                        "Architect performant web interfaces with modern React, TypeScript, and clean CSS design systems.",
                        "Chennai", "Tamil Nadu", "ON_SITE", LocalDate.now().plusDays(50), "https://zoho.com/careers"),

                createOpp("DevOps & SRE Engineer Intern", "Freshworks Inc.",
                        OpportunityType.INTERNSHIP, "Site Reliability",
                        "Automate CI/CD pipelines, observability dashboards, and zero-downtime rolling updates.",
                        "Chennai", "Tamil Nadu", "HYBRID", LocalDate.now().plusDays(22), "https://freshworks.com/careers"),

                createOpp("Cyber Threat Intelligence Researcher", "Quick Heal Technologies",
                        OpportunityType.JOB, "Threat Intelligence",
                        "Analyze ransomware telemetry, malicious payloads, and publish threat advisories.",
                        "Pune", "Maharashtra", "ON_SITE", LocalDate.now().plusDays(32), "https://quickheal.com/careers"),

                createOpp("CodeNation University Coding Competition", "CodeNation Innovation Labs",
                        OpportunityType.CODING_COMPETITION, "Algorithms & DS",
                        "Solve challenging algorithmic puzzles and dynamic programming problems in under 3 hours.",
                        "Bangalore", "Karnataka", "REMOTE", LocalDate.now().plusDays(12), "https://codenation.co.in"),

                createOpp("Embedded Systems & IoT Engineer", "Robert Bosch India",
                        OpportunityType.JOB, "Hardware & Embedded",
                        "Develop automotive sensor drivers and real-time RTOS firmware in C/C++.",
                        "Coimbatore", "Tamil Nadu", "ON_SITE", LocalDate.now().plusDays(42), "https://bosch.in/careers"),

                createOpp("Global Tech Innovator Challenge", "KPMG India",
                        OpportunityType.INNOVATION_CHALLENGE, "Enterprise Tech",
                        "Pitch innovative enterprise SaaS solutions directly to industry C-level executives.",
                        "Gurgaon", "Haryana", "HYBRID", LocalDate.now().plusDays(30), "https://kpmg.com/in/innovator"),

                createOpp("Database Administrator Trainee", "Oracle India",
                        OpportunityType.INTERNSHIP, "Database Administration",
                        "Tune query execution plans, configure database replication, and manage high-availability clusters.",
                        "Hyderabad", "Telangana", "ON_SITE", LocalDate.now().plusDays(19), "https://oracle.com/careers"),

                createOpp("National Tech & Cybersecurity Quiz", "IEEE Computer Society",
                        OpportunityType.QUIZ, "Tech Knowledge",
                        "Compete against top engineering colleges in trivia covering computer networks and systems.",
                        "Delhi", "Delhi", "REMOTE", LocalDate.now().plusDays(10), "https://ieee.org/events/quiz"),

                createOpp("Autonomous Driving AI Research Project", "Mercedes-Benz R&D India",
                        OpportunityType.RESEARCH_COMPETITION, "Computer Vision",
                        "Implement real-time LiDAR object detection and path planning neural network architectures.",
                        "Bangalore", "Karnataka", "ON_SITE", LocalDate.now().plusDays(55), "https://mbrdi.co.in"),

                createOpp("NextGen Women in Engineering Scholarship", "Adobe Systems India",
                        OpportunityType.SCHOLARSHIP, "Diversity in Tech",
                        "Tuition grant and direct mentorship program for aspiring female software engineers.",
                        "Noida", "Uttar Pradesh", "REMOTE", LocalDate.now().plusDays(38), "https://adobe.com/careers"),

                createOpp("Open Source Web Security CTF", "OWASP India Chapter",
                        OpportunityType.CTF_COMPETITION, "Application Security",
                        "Solve OWASP Top 10 web security challenges including SQL injection, SSRF, and auth bypasses.",
                        "Mumbai", "Maharashtra", "REMOTE", LocalDate.now().plusDays(14), "https://owasp.org/ctf")
        );

        oppList.forEach(o -> o.setRequiredSkills(javaSkills));
        opportunityRepo.saveAll(oppList);
        log.info("Seeded {} realistic sample opportunities spanning multiple categories and locations", oppList.size());
    }

    private Opportunity createOpp(String title, String org, OpportunityType type, String roleFamily,
                                 String desc, String city, String state, String workMode,
                                 LocalDate deadline, String url) {
        Opportunity o = new Opportunity();
        o.setTitle(title);
        o.setOrganization(org);
        o.setType(type);
        o.setRoleFamily(roleFamily);
        o.setDescription(desc);
        o.setCity(city);
        o.setState(state);
        o.setLocation(city + ", " + state);
        o.setWorkMode(workMode);
        o.setDeadline(deadline);
        o.setRegistrationDeadline(deadline);
        o.setUrl(url);
        o.setExternalId("EXT-" + Math.abs(title.hashCode()));
        o.setStatus("ACTIVE");
        o.setActive(true);
        o.setSource("CAREEROPS_SAMPLE");
        return o;
    }

    private void seedDemoUserAndApplications() {
        String demoEmail = "candidate@careerops.dev";
        User user = userRepo.findByEmailIgnoreCase(demoEmail).orElseGet(() -> {
            User newUser = new User("Sachin Candidate", demoEmail, passwordEncoder.encode("password123"), "ROLE_USER");
            return userRepo.save(newUser);
        });

        Candidate candidate = candidateRepo.findByEmailIgnoreCase(demoEmail).orElseGet(() -> {
            Candidate newCand = new Candidate();
            newCand.setName("Sachin Candidate");
            newCand.setEmail(demoEmail);
            newCand.setPasswordHash(passwordEncoder.encode("password123"));
            newCand.setLocation("Chennai, Tamil Nadu");
            newCand.setCurrentCity("Chennai");
            newCand.setCurrentState("Tamil Nadu");
            newCand.setCurrentCountry("India");
            newCand.setTargetRoles("Java Backend Developer");
            newCand.setExperienceYears(1);
            return candidateRepo.save(newCand);
        });

        String userEmail = "sachin6002@gmail.com";
        userRepo.findByEmailIgnoreCase(userEmail).orElseGet(() -> {
            User newUser = new User("Sachin", userEmail, passwordEncoder.encode("password123"), "ROLE_USER");
            return userRepo.save(newUser);
        });

        candidateRepo.findByEmailIgnoreCase(userEmail).orElseGet(() -> {
            Candidate newCand = new Candidate();
            newCand.setName("Sachin");
            newCand.setEmail(userEmail);
            newCand.setPasswordHash(passwordEncoder.encode("password123"));
            newCand.setLocation("Chennai, Tamil Nadu");
            newCand.setCurrentCity("Chennai");
            newCand.setCurrentState("Tamil Nadu");
            newCand.setCurrentCountry("India");
            newCand.setTargetRoles("Java Backend Developer");
            newCand.setExperienceYears(1);
            return candidateRepo.save(newCand);
        });

        if (applicationRepo.count() == 0 && opportunityRepo.count() > 0) {
            List<Opportunity> opps = opportunityRepo.findAll();
            if (!opps.isEmpty()) {
                Opportunity firstOpp = opps.get(0);
                Application app1 = new Application();
                app1.setUser(user);
                app1.setCandidate(candidate);
                app1.setOpportunity(firstOpp);
                app1.setStatus(ApplicationStatus.APPLIED);
                app1.setAppliedAt(LocalDate.now());
                app1.setNotes("Applied via CareerOps portal; completed initial screening.");
                applicationRepo.save(app1);

                if (opps.size() > 1) {
                    Opportunity secondOpp = opps.get(1);
                    Application app2 = new Application();
                    app2.setUser(user);
                    app2.setCandidate(candidate);
                    app2.setOpportunity(secondOpp);
                    app2.setStatus(ApplicationStatus.INTERVIEW);
                    app2.setAppliedAt(LocalDate.now().minusDays(3));
                    app2.setNotes("Scheduled technical interview round.");
                    applicationRepo.save(app2);
                }
                log.info("Seeded initial sample applications demonstrating User -> Application -> Opportunity relationship");
            }
        }
    }
}
