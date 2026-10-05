package com.careerops.app.controller;

import com.careerops.app.dto.candidate.CandidateResponse;
import com.careerops.app.dto.candidate.UpdateCandidateRequest;
import com.careerops.app.entity.Candidate;
import com.careerops.app.service.CandidateService;
import com.careerops.app.service.ResumeParserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/candidates")
@Tag(name = "Candidates", description = "Candidate profile management, resume upload, and skill association")
@SecurityRequirement(name = "bearerAuth")
public class CandidateController {

    private final CandidateService candidateService;
    private final ResumeParserService resumeParserService;

    public CandidateController(CandidateService candidateService, ResumeParserService resumeParserService) {
        this.candidateService = candidateService;
        this.resumeParserService = resumeParserService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current candidate profile", description = "Retrieves authenticated candidate's profile information, linked skills, and resume metadata")
    public ResponseEntity<CandidateResponse> getProfile(Authentication authentication) {
        Candidate candidate = candidateService.getByEmail(authentication.getName());
        return ResponseEntity.ok(candidateService.toResponse(candidate));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current candidate profile", description = "Updates profile fields such as name, headline, location, education, experience, target roles, and skill IDs")
    public ResponseEntity<CandidateResponse> updateProfile(Authentication authentication,
                                                           @Valid @RequestBody UpdateCandidateRequest req) {
        CandidateResponse response = candidateService.update(authentication.getName(), req);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete current candidate account", description = "Permanently deletes authenticated candidate and all associated applications")
    public ResponseEntity<Void> deleteAccount(Authentication authentication) {
        candidateService.delete(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/me/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and parse resume", description = "Extracts text from PDF or DOCX resume using Apache Tika and saves it directly to the candidate's profile")
    public ResponseEntity<CandidateResponse> uploadResume(Authentication authentication,
                                                          @RequestParam("file") MultipartFile file) {
        String extractedText = resumeParserService.extractText(file);
        CandidateResponse response = candidateService.updateResume(authentication.getName(), file.getOriginalFilename(), extractedText);
        return ResponseEntity.ok(response);
    }
}
