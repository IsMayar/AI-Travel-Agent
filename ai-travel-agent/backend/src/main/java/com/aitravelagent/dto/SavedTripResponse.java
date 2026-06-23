package com.aitravelagent.dto;

import java.time.Instant;

public record SavedTripResponse(
        Long id,
        String userMessage,
        String origin,
        String destination,
        int budget,
        int days,
        boolean favorite,
        Instant createdAt,
        Instant updatedAt
) {
}
