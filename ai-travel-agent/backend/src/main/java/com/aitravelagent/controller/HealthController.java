package com.aitravelagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public HealthResponse health() {
        return new HealthResponse("ok", "ai-travel-agent");
    }

    public record HealthResponse(String status, String app) {
    }
}
