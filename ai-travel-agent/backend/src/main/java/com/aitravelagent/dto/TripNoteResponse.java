package com.aitravelagent.dto;

import java.time.Instant;

public record TripNoteResponse(
        Long id,
        Long tripId,
        String content,
        Instant createdAt
) {
}
