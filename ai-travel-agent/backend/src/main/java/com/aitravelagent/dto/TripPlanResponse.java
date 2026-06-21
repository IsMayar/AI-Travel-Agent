package com.aitravelagent.dto;

import java.util.List;

public record TripPlanResponse(
        String destination,
        String origin,
        int budget,
        int days,
        List<FlightOptionResponse> flightOptions,
        List<HotelOptionResponse> hotelOptions,
        List<ItineraryDayResponse> itinerary
) {
}
