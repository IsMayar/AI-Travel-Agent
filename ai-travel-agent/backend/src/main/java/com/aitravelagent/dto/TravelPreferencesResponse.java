package com.aitravelagent.dto;

public record TravelPreferencesResponse(
        int preferredBudget,
        int preferredDuration,
        String preferredTravelStyle,
        String preferredDestination
) {
}
