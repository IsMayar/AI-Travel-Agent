package com.aitravelagent.dto;

import java.time.Instant;
import java.time.LocalTime;

public record TripItineraryItemResponse(
        Long id,
        Long tripId,
        int dayNumber,
        String title,
        String description,
        String location,
        LocalTime startTime,
        LocalTime endTime,
        Instant createdAt,
        Instant updatedAt
) {
}
