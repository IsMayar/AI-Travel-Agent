package com.aitravelagent.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.aitravelagent.dto.TripPlanRequest;
import com.aitravelagent.dto.TripPlanResponse;
import com.aitravelagent.service.TripPlanService;

@RestController
public class TripPlanController {

    private final TripPlanService tripPlanService;

    public TripPlanController(TripPlanService tripPlanService) {
        this.tripPlanService = tripPlanService;
    }

    @PostMapping("/api/trips/plan")
    public TripPlanResponse planTrip(@RequestBody TripPlanRequest request) {
        return tripPlanService.planTrip(request);
    }
}
