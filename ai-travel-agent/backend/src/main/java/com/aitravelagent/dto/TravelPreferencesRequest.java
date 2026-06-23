package com.aitravelagent.dto;

public record TravelPreferencesRequest(
        int preferredBudget,
        int preferredDuration,
        String preferredTravelStyle,
        String preferredDestination
) {
}
