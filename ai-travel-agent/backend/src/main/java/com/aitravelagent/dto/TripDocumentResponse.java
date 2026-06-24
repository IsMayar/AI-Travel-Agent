package com.aitravelagent.dto;

import java.time.Instant;

public record TripDocumentResponse(
        Long id,
        Long tripId,
        String name,
        String type,
        String url,
        Instant createdAt
) {
}
