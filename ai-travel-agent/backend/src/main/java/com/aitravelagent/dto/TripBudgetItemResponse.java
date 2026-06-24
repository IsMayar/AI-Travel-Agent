package com.aitravelagent.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TripBudgetItemResponse(
        Long id,
        Long tripId,
        String title,
        String category,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt
) {
}
