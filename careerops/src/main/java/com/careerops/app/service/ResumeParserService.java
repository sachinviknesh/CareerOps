package com.careerops.app.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Service
public class ResumeParserService {

    private static final Logger log = LoggerFactory.getLogger(ResumeParserService.class);

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword"
    );

    private final Tika tika = new Tika();

    public String extractText(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new com.careerops.app.exception.BadRequestException(
                    "Only PDF and DOCX files are supported. Received: " + contentType);
        }
        try {
            String text = tika.parseToString(file.getInputStream());
            log.info("Extracted {} characters from resume: {}", text.length(), file.getOriginalFilename());
            return text;
        } catch (IOException | TikaException e) {
            log.error("Failed to parse resume {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new com.careerops.app.exception.BadRequestException(
                    "Could not extract text from the uploaded file. Please ensure it is a valid PDF or DOCX.");
        }
    }
}
