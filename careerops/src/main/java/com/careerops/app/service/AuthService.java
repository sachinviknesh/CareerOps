package com.careerops.app.service;

import com.careerops.app.dto.auth.AuthResponse;
import com.careerops.app.dto.auth.LoginRequest;
import com.careerops.app.dto.auth.RegisterRequest;
import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.User;
import com.careerops.app.exception.ConflictException;
import com.careerops.app.exception.UnauthorizedException;
import com.careerops.app.repository.CandidateRepository;
import com.careerops.app.repository.UserRepository;
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
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(CandidateRepository candidateRepo,
                       UserRepository userRepo,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.candidateRepo = candidateRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String normalizedEmail = req.email() != null ? req.email().trim().toLowerCase() : "";
        if (candidateRepo.existsByEmailIgnoreCase(normalizedEmail) || userRepo.existsByEmailIgnoreCase(normalizedEmail)) {
            log.warn("Registration attempt with existing email: {}", normalizedEmail);
            throw new ConflictException("An account with this email already exists.");
        }

        String encodedPassword = passwordEncoder.encode(req.password());

        Candidate candidate = new Candidate();
        candidate.setName(req.name() != null ? req.name().trim() : "");
        candidate.setEmail(normalizedEmail);
        candidate.setPasswordHash(encodedPassword);
        candidateRepo.save(candidate);

        // Also save to User entity/table for academic compliance
        User user = new User(candidate.getName(), normalizedEmail, encodedPassword, "ROLE_USER");
        userRepo.save(user);

        log.info("New candidate/user registered successfully: {}", normalizedEmail);
        String token = jwtService.generate(candidate.getEmail());
        return new AuthResponse(token, jwtService.getExpirationMinutes() * 60);
    }

    public AuthResponse login(LoginRequest req) {
        String normalizedEmail = req.email() != null ? req.email().trim().toLowerCase() : "";

        // Attempt authentication via candidate repo
        var candidateOpt = candidateRepo.findByEmailIgnoreCase(normalizedEmail);
        if (candidateOpt.isPresent()) {
            Candidate candidate = candidateOpt.get();
            if (!passwordEncoder.matches(req.password(), candidate.getPasswordHash())) {
                log.warn("Login failed — wrong password for: {}", normalizedEmail);
                throw new UnauthorizedException("Invalid email or password");
            }
            log.info("Login success for candidate: {}", normalizedEmail);
            String token = jwtService.generate(candidate.getEmail());
            return new AuthResponse(token, jwtService.getExpirationMinutes() * 60);
        }

        // Fallback to user repo
        User user = userRepo.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Login failed — email not found: {}", normalizedEmail);
                    return new UnauthorizedException("Invalid email or password");
                });

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            log.warn("Login failed — wrong password for user: {}", normalizedEmail);
            throw new UnauthorizedException("Invalid email or password");
        }

        log.info("Login success for user: {}", normalizedEmail);
        String token = jwtService.generate(user.getEmail());
        return new AuthResponse(token, jwtService.getExpirationMinutes() * 60);
    }
}
