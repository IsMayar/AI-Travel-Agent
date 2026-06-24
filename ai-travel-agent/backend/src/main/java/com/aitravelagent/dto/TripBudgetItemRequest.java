package com.aitravelagent.dto;

import java.math.BigDecimal;

public record TripBudgetItemRequest(
        String title,
        String category,
        BigDecimal amount
) {
}
