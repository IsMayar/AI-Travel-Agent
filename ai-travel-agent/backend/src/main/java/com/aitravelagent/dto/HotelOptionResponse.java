package com.aitravelagent.dto;

public record HotelOptionResponse(
        String name,
        int pricePerNight,
        double rating
) {
}
