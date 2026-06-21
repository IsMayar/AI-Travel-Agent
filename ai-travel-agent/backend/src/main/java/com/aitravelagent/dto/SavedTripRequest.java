package com.aitravelagent.dto;

public record SavedTripRequest(
        String userMessage,
        String origin,
        String destination,
        int budget,
        int days
) {
}
