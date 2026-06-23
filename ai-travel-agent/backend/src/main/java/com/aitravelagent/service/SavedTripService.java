package com.aitravelagent.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.aitravelagent.dto.SavedTripRequest;
import com.aitravelagent.dto.SavedTripResponse;
import com.aitravelagent.dto.TravelPreferencesResponse;
import com.aitravelagent.dto.TripRecommendationResponse;
import com.aitravelagent.dto.TripRecommendationsResponse;
import com.aitravelagent.dto.TripStatsResponse;
import com.aitravelagent.entity.SavedTrip;
import com.aitravelagent.repository.SavedTripRepository;

@Service
public class SavedTripService {

    private final SavedTripRepository savedTripRepository;
    private final TravelPreferencesService travelPreferencesService;

    public SavedTripService(
            SavedTripRepository savedTripRepository,
            TravelPreferencesService travelPreferencesService
    ) {
        this.savedTripRepository = savedTripRepository;
        this.travelPreferencesService = travelPreferencesService;
    }

    public SavedTripResponse saveTrip(SavedTripRequest request) {
        SavedTripRequest safeRequest = request == null
                ? new SavedTripRequest(null, null, null, 0, 0)
                : request;

        SavedTrip savedTrip = new SavedTrip();
        savedTrip.setUserMessage(defaultString(safeRequest.userMessage(), "Trip plan request"));
        savedTrip.setOrigin(defaultString(safeRequest.origin(), "Austin"));
        savedTrip.setDestination(defaultString(safeRequest.destination(), "Dubai"));
        savedTrip.setBudget(safeRequest.budget() > 0 ? safeRequest.budget() : 1500);
        savedTrip.setDays(safeRequest.days() > 0 ? safeRequest.days() : 7);
        savedTrip.setFavorite(false);

        return toResponse(savedTripRepository.save(savedTrip));
    }

    public List<SavedTripResponse> getAllTrips(Boolean favorite) {
        List<SavedTrip> savedTrips = favorite == null
                ? savedTripRepository.findAllByOrderByCreatedAtDesc()
                : savedTripRepository.findAllByFavoriteStatusOrderByCreatedAtDesc(favorite);

        return savedTrips
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SavedTripResponse> getRecentTrips() {
        return savedTripRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TripStatsResponse getTripStats() {
        List<SavedTrip> savedTrips = savedTripRepository.findAllByOrderByCreatedAtDesc();
        long totalTrips = savedTrips.size();
        long favoriteTrips = savedTrips.stream()
                .filter(SavedTrip::isFavorite)
                .count();
        double averageBudget = savedTrips.stream()
                .mapToInt(savedTrip -> savedTrip.getBudget() > 0 ? savedTrip.getBudget() : 1500)
                .average()
                .orElse(0);
        String mostCommonDestination = findMostCommonDestination(savedTrips);

        return new TripStatsResponse(
                totalTrips,
                favoriteTrips,
                averageBudget,
                mostCommonDestination
        );
    }

    public TripRecommendationsResponse getRecommendations() {
        List<SavedTrip> savedTrips = savedTripRepository.findAllByOrderByCreatedAtDesc();
        TravelPreferencesResponse preferences = travelPreferencesService.getPreferences();
        String origin = findRecentOrigin(savedTrips);
        String travelStyle = defaultString(preferences.preferredTravelStyle(), "Relaxed");
        int budget = preferences.preferredBudget() > 0
                ? preferences.preferredBudget()
                : averageBudget(savedTrips);
        int days = preferences.preferredDuration() > 0
                ? preferences.preferredDuration()
                : averageDays(savedTrips);
        List<String> destinations = new ArrayList<>();

        addDestination(destinations, preferences.preferredDestination());
        addDestination(destinations, findMostCommonDestination(savedTrips));
        addDestination(destinations, destinationForTravelStyle(travelStyle));

        for (SavedTrip savedTrip : savedTrips) {
            addDestination(destinations, savedTrip.getDestination());
            if (destinations.size() == 3) {
                break;
            }
        }

        for (String destination : List.of("Dubai", "Tokyo", "Paris", "Lisbon", "Seoul")) {
            addDestination(destinations, destination);
            if (destinations.size() == 3) {
                break;
            }
        }

        List<TripRecommendationResponse> recommendations = new ArrayList<>();
        for (int index = 0; index < 3; index++) {
            recommendations.add(new TripRecommendationResponse(
                    origin,
                    destinations.get(index),
                    budget + (index * 150),
                    days + index,
                    travelStyle,
                    recommendationReason(index)
            ));
        }

        return new TripRecommendationsResponse(recommendations);
    }

    public Optional<SavedTripResponse> getTripById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return savedTripRepository.findById(id)
                .map(this::toResponse);
    }

    public Optional<SavedTripResponse> updateTrip(Long id, SavedTripRequest request) {
        if (id == null) {
            return Optional.empty();
        }

        return savedTripRepository.findById(id)
                .map(savedTrip -> {
                    SavedTripRequest safeRequest = request == null
                            ? new SavedTripRequest(null, null, null, 0, 0)
                            : request;

                    savedTrip.setUserMessage(defaultString(
                            safeRequest.userMessage(),
                            defaultString(savedTrip.getUserMessage(), "Trip plan request")
                    ));
                    savedTrip.setOrigin(defaultString(
                            safeRequest.origin(),
                            defaultString(savedTrip.getOrigin(), "Austin")
                    ));
                    savedTrip.setDestination(defaultString(
                            safeRequest.destination(),
                            defaultString(savedTrip.getDestination(), "Dubai")
                    ));
                    savedTrip.setBudget(safeRequest.budget() > 0 ? safeRequest.budget() : savedTrip.getBudget());
                    savedTrip.setDays(safeRequest.days() > 0 ? safeRequest.days() : savedTrip.getDays());

                    return toResponse(savedTripRepository.save(savedTrip));
                });
    }

    public Optional<SavedTripResponse> toggleFavorite(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return savedTripRepository.findById(id)
                .map(savedTrip -> {
                    savedTrip.setFavorite(!savedTrip.isFavorite());
                    return toResponse(savedTripRepository.save(savedTrip));
                });
    }

    public Optional<SavedTripResponse> duplicateTrip(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return savedTripRepository.findById(id)
                .map(savedTrip -> {
                    SavedTrip duplicate = new SavedTrip();
                    duplicate.setUserMessage(defaultString(savedTrip.getUserMessage(), "Trip plan request"));
                    duplicate.setOrigin(defaultString(savedTrip.getOrigin(), "Austin"));
                    duplicate.setDestination(defaultString(savedTrip.getDestination(), "Dubai"));
                    duplicate.setBudget(savedTrip.getBudget() > 0 ? savedTrip.getBudget() : 1500);
                    duplicate.setDays(savedTrip.getDays() > 0 ? savedTrip.getDays() : 7);
                    duplicate.setFavorite(savedTrip.isFavorite());

                    return toResponse(savedTripRepository.save(duplicate));
                });
    }

    public List<SavedTripResponse> searchTrips(String query) {
        if (query == null || query.isBlank()) {
            return getAllTrips(null);
        }

        String trimmedQuery = query.trim();
        return savedTripRepository.searchTrips(trimmedQuery)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public boolean deleteTripById(Long id) {
        if (id == null || !savedTripRepository.existsById(id)) {
            return false;
        }

        savedTripRepository.deleteById(id);
        return true;
    }

    private SavedTripResponse toResponse(SavedTrip savedTrip) {
        Instant createdAt = savedTrip.getCreatedAt();
        Instant updatedAt = savedTrip.getUpdatedAt();
        Instant safeCreatedAt = createdAt != null
                ? createdAt
                : Optional.ofNullable(updatedAt).orElseGet(Instant::now);
        Instant safeUpdatedAt = updatedAt != null ? updatedAt : safeCreatedAt;

        return new SavedTripResponse(
                savedTrip.getId(),
                defaultString(savedTrip.getUserMessage(), "Trip plan request"),
                defaultString(savedTrip.getOrigin(), "Austin"),
                defaultString(savedTrip.getDestination(), "Dubai"),
                savedTrip.getBudget() > 0 ? savedTrip.getBudget() : 1500,
                savedTrip.getDays() > 0 ? savedTrip.getDays() : 7,
                savedTrip.isFavorite(),
                safeCreatedAt,
                safeUpdatedAt
        );
    }

    private String defaultString(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private String findMostCommonDestination(List<SavedTrip> savedTrips) {
        Map<String, Long> destinationCounts = new LinkedHashMap<>();

        for (SavedTrip savedTrip : savedTrips) {
            String destination = defaultString(savedTrip.getDestination(), "Dubai");
            destinationCounts.put(destination, destinationCounts.getOrDefault(destination, 0L) + 1);
        }

        String mostCommonDestination = "";
        long highestCount = 0;
        for (Map.Entry<String, Long> entry : destinationCounts.entrySet()) {
            if (entry.getValue() > highestCount) {
                mostCommonDestination = entry.getKey();
                highestCount = entry.getValue();
            }
        }

        return mostCommonDestination;
    }

    private String findRecentOrigin(List<SavedTrip> savedTrips) {
        return savedTrips.stream()
                .map(SavedTrip::getOrigin)
                .filter(origin -> origin != null && !origin.isBlank())
                .findFirst()
                .map(String::trim)
                .orElse("Austin");
    }

    private int averageBudget(List<SavedTrip> savedTrips) {
        return (int) Math.round(savedTrips.stream()
                .mapToInt(savedTrip -> savedTrip.getBudget() > 0 ? savedTrip.getBudget() : 1500)
                .average()
                .orElse(1500));
    }

    private int averageDays(List<SavedTrip> savedTrips) {
        return (int) Math.round(savedTrips.stream()
                .mapToInt(savedTrip -> savedTrip.getDays() > 0 ? savedTrip.getDays() : 7)
                .average()
                .orElse(7));
    }

    private void addDestination(List<String> destinations, String destination) {
        String safeDestination = defaultString(destination, "");
        if (safeDestination.isBlank()) {
            return;
        }

        boolean alreadyAdded = destinations.stream()
                .anyMatch(existingDestination -> existingDestination.equalsIgnoreCase(safeDestination));
        if (!alreadyAdded) {
            destinations.add(safeDestination);
        }
    }

    private String destinationForTravelStyle(String travelStyle) {
        String safeStyle = defaultString(travelStyle, "Relaxed").toLowerCase();

        if (safeStyle.contains("adventure")) {
            return "Cape Town";
        }
        if (safeStyle.contains("culture")) {
            return "Kyoto";
        }
        if (safeStyle.contains("family")) {
            return "Orlando";
        }
        if (safeStyle.contains("luxury")) {
            return "Maldives";
        }
        if (safeStyle.contains("budget")) {
            return "Lisbon";
        }

        return "Bali";
    }

    private String recommendationReason(int index) {
        return switch (index) {
            case 0 -> "Matches your saved travel preferences.";
            case 1 -> "Inspired by your saved trip history.";
            default -> "A mock idea for your preferred travel style.";
        };
    }

}
