package com.aitravelagent.dto;

public record FlightOptionResponse(
        String airline,
        int price,
        String duration
) {
}
