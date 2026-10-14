package com.careerops.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "System", description = "System health and status endpoints")
public class RootController {

    @GetMapping("/")
    @Operation(summary = "Root health check", description = "Returns service availability status and application name")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "CareerOps"
        ));
    }
}
