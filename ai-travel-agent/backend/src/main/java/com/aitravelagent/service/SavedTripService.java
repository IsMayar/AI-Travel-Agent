package com.aitravelagent.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.aitravelagent.dto.SavedTripRequest;
import com.aitravelagent.dto.SavedTripResponse;
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

        return toResponse(savedTripRepository.save(savedTrip));
    }

    public List<SavedTripResponse> getSavedTrips() {
        return savedTripRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<SavedTripResponse> getTripById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return savedTripRepository.findById(id)
                .map(this::toResponse);
    }

    public boolean deleteTripById(Long id) {
        if (id == null || !savedTripRepository.existsById(id)) {
            return false;
        }

        savedTripRepository.deleteById(id);
        return true;
    }

    private SavedTripResponse toResponse(SavedTrip savedTrip) {
        return new SavedTripResponse(
                savedTrip.getId(),
                savedTrip.getUserMessage(),
                savedTrip.getOrigin(),
                savedTrip.getDestination(),
                savedTrip.getBudget(),
                savedTrip.getDays(),
                savedTrip.getCreatedAt()
        );
    }

    private String defaultString(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
