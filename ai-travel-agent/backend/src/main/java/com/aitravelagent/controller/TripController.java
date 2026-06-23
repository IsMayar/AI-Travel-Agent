package com.aitravelagent.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aitravelagent.dto.SavedTripRequest;
import com.aitravelagent.dto.SavedTripResponse;
import com.aitravelagent.dto.TripPlanRequest;
import com.aitravelagent.dto.TripPlanResponse;
import com.aitravelagent.service.SavedTripService;
import com.aitravelagent.service.TripPlanService;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripPlanService tripPlanService;
    private final SavedTripService savedTripService;

    public TripController(TripPlanService tripPlanService, SavedTripService savedTripService) {
        this.tripPlanService = tripPlanService;
        this.savedTripService = savedTripService;
    }

    @PostMapping("/plan")
    public TripPlanResponse planTrip(@RequestBody TripPlanRequest request) {
        return tripPlanService.planTrip(request);
    }

    @PostMapping("/save")
    public SavedTripResponse saveTrip(@RequestBody SavedTripRequest request) {
        return savedTripService.saveTrip(request);
    }

    @GetMapping
    public List<SavedTripResponse> getTrips() {
        return savedTripService.getSavedTrips();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        if (!savedTripService.deleteTripById(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
