package com.aitravelagent.dto;

import java.time.Instant;

public record TripTagResponse(
        Long id,
        Long tripId,
        String name,
        Instant createdAt
) {
}
