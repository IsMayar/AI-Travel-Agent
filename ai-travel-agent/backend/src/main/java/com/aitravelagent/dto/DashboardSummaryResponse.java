package com.aitravelagent.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        long totalTrips,
        long favoriteTrips,
        long totalNotes,
        long totalChecklistItems,
        long completedChecklistItems,
        long totalDocuments,
        BigDecimal totalBudgetAmount,
        long totalItineraryItems,
        long totalTags,
        List<SavedTripResponse> recentTrips
) {
}
