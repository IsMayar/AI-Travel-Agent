package com.aitravelagent.dto;

public record TripRecommendationResponse(
        String origin,
        String destination,
        int budget,
        int days,
        String travelStyle,
        String reason
) {
}
