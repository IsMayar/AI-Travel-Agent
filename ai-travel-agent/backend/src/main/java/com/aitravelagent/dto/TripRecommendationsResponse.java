package com.aitravelagent.dto;

import java.util.List;

public record TripRecommendationsResponse(
        List<TripRecommendationResponse> recommendations
) {
}
