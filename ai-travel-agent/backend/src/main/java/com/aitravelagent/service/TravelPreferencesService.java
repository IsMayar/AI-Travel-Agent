package com.aitravelagent.service;

import org.springframework.stereotype.Service;

import com.aitravelagent.dto.TravelPreferencesRequest;
import com.aitravelagent.dto.TravelPreferencesResponse;
import com.aitravelagent.entity.TravelPreferences;
import com.aitravelagent.repository.TravelPreferencesRepository;

@Service
public class TravelPreferencesService {

    private static final int DEFAULT_BUDGET = 1500;
    private static final int DEFAULT_DURATION = 7;
    private static final String DEFAULT_TRAVEL_STYLE = "Relaxed";
    private static final String DEFAULT_DESTINATION = "Dubai";

    private final TravelPreferencesRepository travelPreferencesRepository;

    public TravelPreferencesService(TravelPreferencesRepository travelPreferencesRepository) {
        this.travelPreferencesRepository = travelPreferencesRepository;
    }

    public TravelPreferencesResponse getPreferences() {
        return travelPreferencesRepository.findTopByOrderByIdAsc()
                .map(this::toResponse)
                .orElseGet(this::defaultResponse);
    }

    public TravelPreferencesResponse updatePreferences(TravelPreferencesRequest request) {
        TravelPreferencesRequest safeRequest = request == null
                ? new TravelPreferencesRequest(0, 0, null, null)
                : request;
        TravelPreferences preferences = travelPreferencesRepository
                .findTopByOrderByIdAsc()
                .orElseGet(TravelPreferences::new);

        preferences.setPreferredBudget(
                safeRequest.preferredBudget() > 0 ? safeRequest.preferredBudget() : DEFAULT_BUDGET
        );
        preferences.setPreferredDuration(
                safeRequest.preferredDuration() > 0 ? safeRequest.preferredDuration() : DEFAULT_DURATION
        );
        preferences.setPreferredTravelStyle(defaultString(
                safeRequest.preferredTravelStyle(),
                DEFAULT_TRAVEL_STYLE
        ));
        preferences.setPreferredDestination(defaultString(
                safeRequest.preferredDestination(),
                DEFAULT_DESTINATION
        ));

        return toResponse(travelPreferencesRepository.save(preferences));
    }

    private TravelPreferencesResponse toResponse(TravelPreferences preferences) {
        return new TravelPreferencesResponse(
                preferences.getPreferredBudget() > 0 ? preferences.getPreferredBudget() : DEFAULT_BUDGET,
                preferences.getPreferredDuration() > 0 ? preferences.getPreferredDuration() : DEFAULT_DURATION,
                defaultString(preferences.getPreferredTravelStyle(), DEFAULT_TRAVEL_STYLE),
                defaultString(preferences.getPreferredDestination(), DEFAULT_DESTINATION)
        );
    }

    private TravelPreferencesResponse defaultResponse() {
        return new TravelPreferencesResponse(
                DEFAULT_BUDGET,
                DEFAULT_DURATION,
                DEFAULT_TRAVEL_STYLE,
                DEFAULT_DESTINATION
        );
    }

    private String defaultString(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
