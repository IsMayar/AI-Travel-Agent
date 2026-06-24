package com.aitravelagent.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.aitravelagent.dto.DashboardSummaryResponse;
import com.aitravelagent.entity.SavedTrip;
import com.aitravelagent.entity.TripBudgetItem;
import com.aitravelagent.repository.SavedTripRepository;
import com.aitravelagent.repository.TripBudgetItemRepository;
import com.aitravelagent.repository.TripChecklistItemRepository;
import com.aitravelagent.repository.TripDocumentRepository;
import com.aitravelagent.repository.TripItineraryItemRepository;
import com.aitravelagent.repository.TripNoteRepository;
import com.aitravelagent.repository.TripTagRepository;

@Service
public class DashboardService {

    private final SavedTripRepository savedTripRepository;
    private final TripNoteRepository tripNoteRepository;
    private final TripChecklistItemRepository tripChecklistItemRepository;
    private final TripDocumentRepository tripDocumentRepository;
    private final TripBudgetItemRepository tripBudgetItemRepository;
    private final TripItineraryItemRepository tripItineraryItemRepository;
    private final TripTagRepository tripTagRepository;
    private final SavedTripService savedTripService;

    public DashboardService(
            SavedTripRepository savedTripRepository,
            TripNoteRepository tripNoteRepository,
            TripChecklistItemRepository tripChecklistItemRepository,
            TripDocumentRepository tripDocumentRepository,
            TripBudgetItemRepository tripBudgetItemRepository,
            TripItineraryItemRepository tripItineraryItemRepository,
            TripTagRepository tripTagRepository,
            SavedTripService savedTripService
    ) {
        this.savedTripRepository = savedTripRepository;
        this.tripNoteRepository = tripNoteRepository;
        this.tripChecklistItemRepository = tripChecklistItemRepository;
        this.tripDocumentRepository = tripDocumentRepository;
        this.tripBudgetItemRepository = tripBudgetItemRepository;
        this.tripItineraryItemRepository = tripItineraryItemRepository;
        this.tripTagRepository = tripTagRepository;
        this.savedTripService = savedTripService;
    }

    public DashboardSummaryResponse getSummary() {
        long totalTrips = savedTripRepository.count();
        long favoriteTrips = savedTripRepository.findAll()
                .stream()
                .filter(SavedTrip::isFavorite)
                .count();
        BigDecimal totalBudgetAmount = tripBudgetItemRepository.findAll()
                .stream()
                .map(TripBudgetItem::getAmount)
                .map(amount -> amount == null ? BigDecimal.ZERO : amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardSummaryResponse(
                totalTrips,
                favoriteTrips,
                tripNoteRepository.count(),
                tripChecklistItemRepository.count(),
                tripChecklistItemRepository.countByCompletedTrue(),
                tripDocumentRepository.count(),
                totalBudgetAmount,
                tripItineraryItemRepository.count(),
                tripTagRepository.count(),
                savedTripService.getRecentTrips()
        );
    }
}
