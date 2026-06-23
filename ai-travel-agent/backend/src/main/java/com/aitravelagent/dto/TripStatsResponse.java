package com.aitravelagent.dto;

public record TripStatsResponse(
        long totalTrips,
        long favoriteTrips,
        double averageBudget,
        String mostCommonDestination
) {
}
