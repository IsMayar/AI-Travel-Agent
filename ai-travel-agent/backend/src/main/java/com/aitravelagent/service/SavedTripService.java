package com.aitravelagent.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.aitravelagent.dto.SavedTripRequest;
import com.aitravelagent.dto.SavedTripResponse;
import com.aitravelagent.dto.TripStatsResponse;
import com.aitravelagent.entity.SavedTrip;
import com.aitravelagent.repository.SavedTripRepository;

@Service
public class SavedTripService {

    private final SavedTripRepository savedTripRepository;

    public SavedTripService(SavedTripRepository savedTripRepository) {
        this.savedTripRepository = savedTripRepository;
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
}
