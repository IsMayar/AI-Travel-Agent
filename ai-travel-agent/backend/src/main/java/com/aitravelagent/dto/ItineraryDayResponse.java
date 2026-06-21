package com.aitravelagent.dto;

import java.util.List;

public record ItineraryDayResponse(
        int day,
        String title,
        List<String> activities
) {
}
