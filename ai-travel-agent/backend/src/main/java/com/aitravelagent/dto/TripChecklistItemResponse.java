package com.aitravelagent.dto;

import java.time.Instant;

public record TripChecklistItemResponse(
        Long id,
        Long tripId,
        String title,
        boolean completed,
        Instant createdAt
) {
}
