package com.aitravelagent.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aitravelagent.dto.FlightOptionResponse;
import com.aitravelagent.dto.HotelOptionResponse;
import com.aitravelagent.dto.ItineraryDayResponse;
import com.aitravelagent.dto.TripPlanRequest;
import com.aitravelagent.dto.TripPlanResponse;

@Service
public class TripPlanService {

    public TripPlanResponse planTrip(TripPlanRequest request) {
        return new TripPlanResponse(
                "Dubai",
                "Austin",
                1500,
                7,
                List.of(new FlightOptionResponse(
                        "Mock Airline",
                        850,
                        "18h 30m"
                )),
                List.of(new HotelOptionResponse(
                        "Mock Hotel Dubai",
                        90,
                        4.3
                )),
                List.of(new ItineraryDayResponse(
                        1,
                        "Arrival and hotel check-in",
                        List.of("Arrive in Dubai", "Check into hotel", "Evening walk")
                ))
        );
    }
}
