package com.careerops.app.service;

import com.careerops.app.dto.auth.AuthResponse;
import com.careerops.app.dto.auth.LoginRequest;
import com.careerops.app.dto.auth.RegisterRequest;
import com.careerops.app.entity.Candidate;
import com.careerops.app.exception.BadRequestException;
import com.careerops.app.repository.CandidateRepository;
import com.careerops.app.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final CandidateRepository candidateRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(CandidateRepository candidateRepo,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.candidateRepo = candidateRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (candidateRepo.existsByEmailIgnoreCase(req.email())) {
            log.warn("Registration attempt with existing email: {}", req.email());
            throw new BadRequestException("An account with this email already exists.");
        }

        Candidate candidate = new Candidate();
        candidate.setName(req.name());
        candidate.setEmail(req.email().toLowerCase());
        candidate.setPasswordHash(passwordEncoder.encode(req.password()));
        candidateRepo.save(candidate);

        log.info("New candidate registered: {}", req.email());
        String token = jwtService.generate(candidate.getEmail());
        return new AuthResponse(token, jwtService.getExpirationMinutes() * 60);
    }

    public AuthResponse login(LoginRequest req) {
        Candidate candidate = candidateRepo.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> {
                    log.warn("Login failed — email not found: {}", req.email());
                    return new BadRequestException("Invalid email or password.");
                });

        if (!passwordEncoder.matches(req.password(), candidate.getPasswordHash())) {
            log.warn("Login failed — wrong password for: {}", req.email());
            throw new BadRequestException("Invalid email or password.");
        }

        log.info("Login success: {}", req.email());
        String token = jwtService.generate(candidate.getEmail());
        return new AuthResponse(token, jwtService.getExpirationMinutes() * 60);
    }
}
