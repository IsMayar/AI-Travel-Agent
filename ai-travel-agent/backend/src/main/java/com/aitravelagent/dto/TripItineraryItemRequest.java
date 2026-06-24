package com.aitravelagent.dto;

import java.time.LocalTime;

public record TripItineraryItemRequest(
        Integer dayNumber,
        String title,
        String description,
        String location,
        LocalTime startTime,
        LocalTime endTime
) {
}
