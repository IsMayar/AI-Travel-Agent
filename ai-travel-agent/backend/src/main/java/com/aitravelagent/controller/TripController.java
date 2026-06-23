package com.aitravelagent.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aitravelagent.dto.SavedTripRequest;
import com.aitravelagent.dto.SavedTripResponse;
import com.aitravelagent.dto.TripNoteRequest;
import com.aitravelagent.dto.TripNoteResponse;
import com.aitravelagent.dto.TripPlanRequest;
import com.aitravelagent.dto.TripPlanResponse;
import com.aitravelagent.dto.TripRecommendationsResponse;
import com.aitravelagent.dto.TripStatsResponse;
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
    public List<SavedTripResponse> getTrips(@RequestParam(required = false) Boolean favorite) {
        return savedTripService.getAllTrips(favorite);
    }

    @GetMapping("/search")
    public List<SavedTripResponse> searchTrips(@RequestParam(defaultValue = "") String q) {
        return savedTripService.searchTrips(q);
    }

    @GetMapping("/recent")
    public List<SavedTripResponse> getRecentTrips() {
        return savedTripService.getRecentTrips();
    }

    @GetMapping("/stats")
    public TripStatsResponse getTripStats() {
        return savedTripService.getTripStats();
    }

    @GetMapping("/recommendations")
    public TripRecommendationsResponse getRecommendations() {
        return savedTripService.getRecommendations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavedTripResponse> getTrip(@PathVariable Long id) {
        return savedTripService.getTripById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavedTripResponse> updateTrip(
            @PathVariable Long id,
            @RequestBody SavedTripRequest request
    ) {
        return savedTripService.updateTrip(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<SavedTripResponse> toggleFavorite(@PathVariable Long id) {
        return savedTripService.toggleFavorite(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<SavedTripResponse> duplicateTrip(@PathVariable Long id) {
        return savedTripService.duplicateTrip(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<List<TripNoteResponse>> getTripNotes(@PathVariable Long id) {
        return savedTripService.getNotesForTrip(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<TripNoteResponse> addTripNote(
            @PathVariable Long id,
            @RequestBody TripNoteRequest request
    ) {
        return savedTripService.addTripNote(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        if (!savedTripService.deleteTripById(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
