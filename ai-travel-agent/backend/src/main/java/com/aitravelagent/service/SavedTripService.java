package com.aitravelagent.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aitravelagent.dto.SavedTripRequest;
import com.aitravelagent.dto.SavedTripResponse;
import com.aitravelagent.dto.TravelPreferencesResponse;
import com.aitravelagent.dto.TripBudgetItemRequest;
import com.aitravelagent.dto.TripBudgetItemResponse;
import com.aitravelagent.dto.TripChecklistItemRequest;
import com.aitravelagent.dto.TripChecklistItemResponse;
import com.aitravelagent.dto.TripDocumentRequest;
import com.aitravelagent.dto.TripDocumentResponse;
import com.aitravelagent.dto.TripItineraryItemRequest;
import com.aitravelagent.dto.TripItineraryItemResponse;
import com.aitravelagent.dto.TripNoteRequest;
import com.aitravelagent.dto.TripNoteResponse;
import com.aitravelagent.dto.TripNoteUpdateRequest;
import com.aitravelagent.dto.TripRecommendationResponse;
import com.aitravelagent.dto.TripRecommendationsResponse;
import com.aitravelagent.dto.TripStatsResponse;
import com.aitravelagent.dto.TripTagRequest;
import com.aitravelagent.dto.TripTagResponse;
import com.aitravelagent.entity.SavedTrip;
import com.aitravelagent.entity.TripBudgetItem;
import com.aitravelagent.entity.TripChecklistItem;
import com.aitravelagent.entity.TripDocument;
import com.aitravelagent.entity.TripItineraryItem;
import com.aitravelagent.entity.TripNote;
import com.aitravelagent.entity.TripTag;
import com.aitravelagent.repository.SavedTripRepository;
import com.aitravelagent.repository.TripBudgetItemRepository;
import com.aitravelagent.repository.TripChecklistItemRepository;
import com.aitravelagent.repository.TripDocumentRepository;
import com.aitravelagent.repository.TripItineraryItemRepository;
import com.aitravelagent.repository.TripNoteRepository;
import com.aitravelagent.repository.TripTagRepository;

@Service
public class SavedTripService {

    private final SavedTripRepository savedTripRepository;
    private final TravelPreferencesService travelPreferencesService;
    private final TripNoteRepository tripNoteRepository;
    private final TripChecklistItemRepository tripChecklistItemRepository;
    private final TripDocumentRepository tripDocumentRepository;
    private final TripBudgetItemRepository tripBudgetItemRepository;
    private final TripItineraryItemRepository tripItineraryItemRepository;
    private final TripTagRepository tripTagRepository;

    public SavedTripService(
            SavedTripRepository savedTripRepository,
            TravelPreferencesService travelPreferencesService,
            TripNoteRepository tripNoteRepository,
            TripChecklistItemRepository tripChecklistItemRepository,
            TripDocumentRepository tripDocumentRepository,
            TripBudgetItemRepository tripBudgetItemRepository,
            TripItineraryItemRepository tripItineraryItemRepository,
            TripTagRepository tripTagRepository
    ) {
        this.savedTripRepository = savedTripRepository;
        this.travelPreferencesService = travelPreferencesService;
        this.tripNoteRepository = tripNoteRepository;
        this.tripChecklistItemRepository = tripChecklistItemRepository;
        this.tripDocumentRepository = tripDocumentRepository;
        this.tripBudgetItemRepository = tripBudgetItemRepository;
        this.tripItineraryItemRepository = tripItineraryItemRepository;
        this.tripTagRepository = tripTagRepository;
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

    public Optional<TripNoteResponse> addTripNote(Long tripId, TripNoteRequest request) {
        if (tripId == null) {
            return Optional.empty();
        }

        return savedTripRepository.findById(tripId)
                .map(savedTrip -> {
                    TripNoteRequest safeRequest = request == null
                            ? new TripNoteRequest(null)
                            : request;
                    TripNote note = new TripNote();
                    note.setTrip(savedTrip);
                    note.setContent(defaultString(safeRequest.content(), "Trip note"));

                    return toNoteResponse(tripNoteRepository.save(note));
                });
    }

    public Optional<TripNoteResponse> updateNote(
            Long tripId,
            Long noteId,
            TripNoteUpdateRequest request
    ) {
        if (tripId == null || noteId == null || !savedTripRepository.existsById(tripId)) {
            return Optional.empty();
        }

        return tripNoteRepository.findByIdAndTrip_Id(noteId, tripId)
                .map(note -> {
                    TripNoteUpdateRequest safeRequest = request == null
                            ? new TripNoteUpdateRequest(null)
                            : request;
                    note.setContent(defaultString(safeRequest.content(), "Trip note"));

                    return toNoteResponse(tripNoteRepository.save(note), tripId);
                });
    }

    public Optional<List<TripNoteResponse>> getNotesForTrip(Long tripId) {
        if (tripId == null || !savedTripRepository.existsById(tripId)) {
            return Optional.empty();
        }

        List<TripNoteResponse> notes = tripNoteRepository.findByTripIdOrderByCreatedAtDesc(tripId)
                .stream()
                .map(note -> toNoteResponse(note, tripId))
                .toList();

        return Optional.of(notes);
    }

    @Transactional
    public boolean deleteNote(Long tripId, Long noteId) {
        if (tripId == null || noteId == null || !savedTripRepository.existsById(tripId)) {
            return false;
        }

        return tripNoteRepository.findByIdAndTrip_Id(noteId, tripId)
                .map(note -> {
                    tripNoteRepository.delete(note);
                    return true;
                })
                .orElse(false);
    }

    public Optional<List<TripChecklistItemResponse>> getChecklistForTrip(Long tripId) {
        if (tripId == null || !savedTripRepository.existsById(tripId)) {
            return Optional.empty();
        }

        List<TripChecklistItemResponse> checklist = tripChecklistItemRepository
                .findByTripIdOrderByCreatedAtAsc(tripId)
                .stream()
                .map(item -> toChecklistResponse(item, tripId))
                .toList();

        return Optional.of(checklist);
    }

    public Optional<TripChecklistItemResponse> addChecklistItem(
            Long tripId,
            TripChecklistItemRequest request
    ) {
        if (tripId == null) {
            return Optional.empty();
        }

        return savedTripRepository.findById(tripId)
                .map(savedTrip -> {
                    TripChecklistItemRequest safeRequest = request == null
                            ? new TripChecklistItemRequest(null)
                            : request;
                    TripChecklistItem item = new TripChecklistItem();
                    item.setTrip(savedTrip);
                    item.setTitle(defaultString(safeRequest.title(), "Checklist item"));
                    item.setCompleted(false);

                    return toChecklistResponse(tripChecklistItemRepository.save(item));
                });
    }

    public Optional<TripChecklistItemResponse> toggleChecklistItem(Long tripId, Long itemId) {
        if (tripId == null || itemId == null || !savedTripRepository.existsById(tripId)) {
            return Optional.empty();
        }

        return tripChecklistItemRepository.findByIdAndTrip_Id(itemId, tripId)
                .map(item -> {
                    item.setCompleted(!item.isCompleted());
                    return toChecklistResponse(tripChecklistItemRepository.save(item), tripId);
                });
    }

    @Transactional
    public boolean deleteChecklistItem(Long tripId, Long itemId) {
        if (tripId == null || itemId == null || !savedTripRepository.existsById(tripId)) {
            return false;
        }

        return tripChecklistItemRepository.findByIdAndTrip_Id(itemId, tripId)
                .map(item -> {
                    tripChecklistItemRepository.delete(item);
                    return true;
                })
                .orElse(false);
    }

    public Optional<List<TripDocumentResponse>> getDocumentsForTrip(Long tripId) {
        if (tripId == null || !savedTripRepository.existsById(tripId)) {
            return Optional.empty();
        }

        List<TripDocumentResponse> documents = tripDocumentRepository.findByTripIdOrderByCreatedAtDesc(tripId)
                .stream()
                .map(document -> toDocumentResponse(document, tripId))
                .toList();

        return Optional.of(documents);
    }

    public Optional<TripDocumentResponse> addDocument(Long tripId, TripDocumentRequest request) {
        if (tripId == null) {
            return Optional.empty();
        }

        return savedTripRepository.findById(tripId)
                .map(savedTrip -> {
                    TripDocumentRequest safeRequest = request == null
                            ? new TripDocumentRequest(null, null, null)
                            : request;
                    TripDocument document = new TripDocument();
                    document.setTrip(savedTrip);
                    document.setName(defaultString(safeRequest.name(), "Travel document"));
                    document.setType(defaultString(safeRequest.type(), "Link"));
                    document.setUrl(defaultString(safeRequest.url(), "https://example.com"));

                    return toDocumentResponse(tripDocumentRepository.save(document));
                });
    }

    @Transactional
    public boolean deleteDocument(Long tripId, Long documentId) {
        if (tripId == null || documentId == null || !savedTripRepository.existsById(tripId)) {
            return false;
        }

        return tripDocumentRepository.findByIdAndTrip_Id(documentId, tripId)
                .map(document -> {
                    tripDocumentRepository.delete(document);
                    return true;
                })
                .orElse(false);
    }

    public Optional<List<TripBudgetItemResponse>> getBudgetItemsForTrip(Long tripId) {
        if (tripId == null || !savedTripRepository.existsById(tripId)) {
            return Optional.empty();
        }

        List<TripBudgetItemResponse> budgetItems = tripBudgetItemRepository.findByTripIdOrderByCreatedAtAsc(tripId)
                .stream()
                .map(item -> toBudgetItemResponse(item, tripId))
                .toList();

        return Optional.of(budgetItems);
    }

    public Optional<TripBudgetItemResponse> addBudgetItem(Long tripId, TripBudgetItemRequest request) {
        if (tripId == null) {
            return Optional.empty();
        }

        return savedTripRepository.findById(tripId)
                .map(savedTrip -> {
                    TripBudgetItemRequest safeRequest = request == null
                            ? new TripBudgetItemRequest(null, null, null)
                            : request;
                    TripBudgetItem item = new TripBudgetItem();
                    item.setTrip(savedTrip);
                    item.setTitle(defaultString(safeRequest.title(), "Budget item"));
                    item.setCategory(defaultString(safeRequest.category(), "General"));
                    item.setAmount(defaultAmount(safeRequest.amount()));

                    return toBudgetItemResponse(tripBudgetItemRepository.save(item));
                });
    }

    public Optional<TripBudgetItemResponse> updateBudgetItem(
            Long tripId,
            Long itemId,
            TripBudgetItemRequest request
    ) {
        if (tripId == null || itemId == null || !savedTripRepository.existsById(tripId)) {
            return Optional.empty();
        }

        return tripBudgetItemRepository.findByIdAndTrip_Id(itemId, tripId)
                .map(item -> {
                    TripBudgetItemRequest safeRequest = request == null
                            ? new TripBudgetItemRequest(null, null, null)
                            : request;
                    item.setTitle(defaultString(safeRequest.title(), "Budget item"));
                    item.setCategory(defaultString(safeRequest.category(), "General"));
                    item.setAmount(defaultAmount(safeRequest.amount()));

                    return toBudgetItemResponse(tripBudgetItemRepository.save(item), tripId);
                });
    }

    @Transactional
    public boolean deleteBudgetItem(Long tripId, Long itemId) {
        if (tripId == null || itemId == null || !savedTripRepository.existsById(tripId)) {
            return false;
        }

        return tripBudgetItemRepository.findByIdAndTrip_Id(itemId, tripId)
                .map(item -> {
                    tripBudgetItemRepository.delete(item);
                    return true;
                })
                .orElse(false);
    }

    public Optional<List<TripItineraryItemResponse>> getItineraryForTrip(Long tripId) {
        if (tripId == null || !savedTripRepository.existsById(tripId)) {
            return Optional.empty();
        }

        List<TripItineraryItemResponse> itinerary = tripItineraryItemRepository
                .findByTripIdOrderByDayNumberAscStartTimeAsc(tripId)
                .stream()
                .map(item -> toItineraryResponse(item, tripId))
                .toList();

        return Optional.of(itinerary);
    }

    public Optional<TripItineraryItemResponse> addItineraryItem(
            Long tripId,
            TripItineraryItemRequest request
    ) {
        if (tripId == null) {
            return Optional.empty();
        }

        return savedTripRepository.findById(tripId)
                .map(savedTrip -> {
                    TripItineraryItem item = new TripItineraryItem();
                    applyItineraryRequest(item, request);
                    item.setTrip(savedTrip);

                    return toItineraryResponse(tripItineraryItemRepository.save(item));
                });
    }

    public Optional<TripItineraryItemResponse> updateItineraryItem(
            Long tripId,
            Long itemId,
            TripItineraryItemRequest request
    ) {
        if (tripId == null || itemId == null || !savedTripRepository.existsById(tripId)) {
            return Optional.empty();
        }

        return tripItineraryItemRepository.findByIdAndTrip_Id(itemId, tripId)
                .map(item -> {
                    applyItineraryRequest(item, request);
                    return toItineraryResponse(tripItineraryItemRepository.save(item), tripId);
                });
    }

    @Transactional
    public boolean deleteItineraryItem(Long tripId, Long itemId) {
        if (tripId == null || itemId == null || !savedTripRepository.existsById(tripId)) {
            return false;
        }

        return tripItineraryItemRepository.findByIdAndTrip_Id(itemId, tripId)
                .map(item -> {
                    tripItineraryItemRepository.delete(item);
                    return true;
                })
                .orElse(false);
    }

    public Optional<List<TripTagResponse>> getTagsForTrip(Long tripId) {
        if (tripId == null || !savedTripRepository.existsById(tripId)) {
            return Optional.empty();
        }

        List<TripTagResponse> tags = tripTagRepository.findByTripIdOrderByNameAsc(tripId)
                .stream()
                .map(tag -> toTagResponse(tag, tripId))
                .toList();

        return Optional.of(tags);
    }

    public Optional<TripTagResponse> addTag(Long tripId, TripTagRequest request) {
        if (tripId == null) {
            return Optional.empty();
        }

        return savedTripRepository.findById(tripId)
                .map(savedTrip -> {
                    TripTagRequest safeRequest = request == null
                            ? new TripTagRequest(null)
                            : request;
                    String name = defaultString(safeRequest.name(), "Trip tag");

                    return tripTagRepository.findFirstByTrip_IdAndNameIgnoreCase(tripId, name)
                            .map(existingTag -> toTagResponse(existingTag, tripId))
                            .orElseGet(() -> {
                                TripTag tag = new TripTag();
                                tag.setTrip(savedTrip);
                                tag.setName(name);
                                return toTagResponse(tripTagRepository.save(tag));
                            });
                });
    }

    @Transactional
    public boolean deleteTag(Long tripId, Long tagId) {
        if (tripId == null || tagId == null || !savedTripRepository.existsById(tripId)) {
            return false;
        }

        return tripTagRepository.findByIdAndTrip_Id(tagId, tripId)
                .map(tag -> {
                    tripTagRepository.delete(tag);
                    return true;
                })
                .orElse(false);
    }

    public Optional<String> exportTrip(Long tripId) {
        if (tripId == null) {
            return Optional.empty();
        }

        return savedTripRepository.findById(tripId)
                .map(savedTrip -> {
                    SavedTripResponse trip = toResponse(savedTrip);
                    List<TripNoteResponse> notes = tripNoteRepository.findByTripIdOrderByCreatedAtDesc(tripId)
                            .stream()
                            .map(note -> toNoteResponse(note, tripId))
                            .toList();
                    List<TripChecklistItemResponse> checklist = tripChecklistItemRepository
                            .findByTripIdOrderByCreatedAtAsc(tripId)
                            .stream()
                            .map(item -> toChecklistResponse(item, tripId))
                            .toList();
                    List<TripDocumentResponse> documents = tripDocumentRepository
                            .findByTripIdOrderByCreatedAtDesc(tripId)
                            .stream()
                            .map(document -> toDocumentResponse(document, tripId))
                            .toList();
                    List<TripBudgetItemResponse> budgetItems = tripBudgetItemRepository
                            .findByTripIdOrderByCreatedAtAsc(tripId)
                            .stream()
                            .map(item -> toBudgetItemResponse(item, tripId))
                            .toList();
                    List<TripItineraryItemResponse> itinerary = tripItineraryItemRepository
                            .findByTripIdOrderByDayNumberAscStartTimeAsc(tripId)
                            .stream()
                            .map(item -> toItineraryResponse(item, tripId))
                            .toList();
                    List<TripTagResponse> tags = tripTagRepository.findByTripIdOrderByNameAsc(tripId)
                            .stream()
                            .map(tag -> toTagResponse(tag, tripId))
                            .toList();

                    return buildTripExport(trip, notes, checklist, documents, budgetItems, itinerary, tags);
                });
    }

    @Transactional
    public boolean deleteTripById(Long id) {
        if (id == null || !savedTripRepository.existsById(id)) {
            return false;
        }

        tripTagRepository.deleteByTripId(id);
        tripItineraryItemRepository.deleteByTripId(id);
        tripBudgetItemRepository.deleteByTripId(id);
        tripDocumentRepository.deleteByTripId(id);
        tripChecklistItemRepository.deleteByTripId(id);
        tripNoteRepository.deleteByTripId(id);
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

    private TripNoteResponse toNoteResponse(TripNote note) {
        return toNoteResponse(note, null);
    }

    private TripNoteResponse toNoteResponse(TripNote note, Long fallbackTripId) {
        Long tripId = note.getTrip() == null ? fallbackTripId : note.getTrip().getId();
        Instant createdAt = note.getCreatedAt() == null ? Instant.now() : note.getCreatedAt();

        return new TripNoteResponse(
                note.getId(),
                tripId,
                defaultString(note.getContent(), "Trip note"),
                createdAt
        );
    }

    private TripChecklistItemResponse toChecklistResponse(TripChecklistItem item) {
        return toChecklistResponse(item, null);
    }

    private TripChecklistItemResponse toChecklistResponse(TripChecklistItem item, Long fallbackTripId) {
        Long tripId = item.getTrip() == null ? fallbackTripId : item.getTrip().getId();
        Instant createdAt = item.getCreatedAt() == null ? Instant.now() : item.getCreatedAt();

        return new TripChecklistItemResponse(
                item.getId(),
                tripId,
                defaultString(item.getTitle(), "Checklist item"),
                item.isCompleted(),
                createdAt
        );
    }

    private TripDocumentResponse toDocumentResponse(TripDocument document) {
        return toDocumentResponse(document, null);
    }

    private TripDocumentResponse toDocumentResponse(TripDocument document, Long fallbackTripId) {
        Long tripId = document.getTrip() == null ? fallbackTripId : document.getTrip().getId();
        Instant createdAt = document.getCreatedAt() == null ? Instant.now() : document.getCreatedAt();

        return new TripDocumentResponse(
                document.getId(),
                tripId,
                defaultString(document.getName(), "Travel document"),
                defaultString(document.getType(), "Link"),
                defaultString(document.getUrl(), "https://example.com"),
                createdAt
        );
    }

    private TripBudgetItemResponse toBudgetItemResponse(TripBudgetItem item) {
        return toBudgetItemResponse(item, null);
    }

    private TripBudgetItemResponse toBudgetItemResponse(TripBudgetItem item, Long fallbackTripId) {
        Long tripId = item.getTrip() == null ? fallbackTripId : item.getTrip().getId();
        Instant createdAt = item.getCreatedAt() == null ? Instant.now() : item.getCreatedAt();
        Instant updatedAt = item.getUpdatedAt() == null ? createdAt : item.getUpdatedAt();

        return new TripBudgetItemResponse(
                item.getId(),
                tripId,
                defaultString(item.getTitle(), "Budget item"),
                defaultString(item.getCategory(), "General"),
                defaultAmount(item.getAmount()),
                createdAt,
                updatedAt
        );
    }

    private void applyItineraryRequest(TripItineraryItem item, TripItineraryItemRequest request) {
        TripItineraryItemRequest safeRequest = request == null
                ? new TripItineraryItemRequest(null, null, null, null, null, null)
                : request;

        item.setDayNumber(safeRequest.dayNumber() != null && safeRequest.dayNumber() > 0
                ? safeRequest.dayNumber()
                : 1);
        item.setTitle(defaultString(safeRequest.title(), "Itinerary item"));
        item.setDescription(defaultString(safeRequest.description(), "Trip activity"));
        item.setLocation(defaultString(safeRequest.location(), "TBD"));
        item.setStartTime(defaultTime(safeRequest.startTime(), LocalTime.of(9, 0)));
        item.setEndTime(defaultTime(safeRequest.endTime(), LocalTime.of(10, 0)));
    }

    private TripItineraryItemResponse toItineraryResponse(TripItineraryItem item) {
        return toItineraryResponse(item, null);
    }

    private TripItineraryItemResponse toItineraryResponse(TripItineraryItem item, Long fallbackTripId) {
        Long tripId = item.getTrip() == null ? fallbackTripId : item.getTrip().getId();
        Instant createdAt = item.getCreatedAt() == null ? Instant.now() : item.getCreatedAt();
        Instant updatedAt = item.getUpdatedAt() == null ? createdAt : item.getUpdatedAt();

        return new TripItineraryItemResponse(
                item.getId(),
                tripId,
                item.getDayNumber() > 0 ? item.getDayNumber() : 1,
                defaultString(item.getTitle(), "Itinerary item"),
                defaultString(item.getDescription(), "Trip activity"),
                defaultString(item.getLocation(), "TBD"),
                defaultTime(item.getStartTime(), LocalTime.of(9, 0)),
                defaultTime(item.getEndTime(), LocalTime.of(10, 0)),
                createdAt,
                updatedAt
        );
    }

    private TripTagResponse toTagResponse(TripTag tag) {
        return toTagResponse(tag, null);
    }

    private TripTagResponse toTagResponse(TripTag tag, Long fallbackTripId) {
        Long tripId = tag.getTrip() == null ? fallbackTripId : tag.getTrip().getId();
        Instant createdAt = tag.getCreatedAt() == null ? Instant.now() : tag.getCreatedAt();

        return new TripTagResponse(
                tag.getId(),
                tripId,
                defaultString(tag.getName(), "Trip tag"),
                createdAt
        );
    }

    private String buildTripExport(
            SavedTripResponse trip,
            List<TripNoteResponse> notes,
            List<TripChecklistItemResponse> checklist,
            List<TripDocumentResponse> documents,
            List<TripBudgetItemResponse> budgetItems,
            List<TripItineraryItemResponse> itinerary,
            List<TripTagResponse> tags
    ) {
        StringBuilder export = new StringBuilder();
        export.append("AI Travel Agent Trip Export\n");
        export.append("===========================\n\n");
        export.append("Trip\n");
        export.append("----\n");
        export.append("Origin: ").append(trip.origin()).append('\n');
        export.append("Destination: ").append(trip.destination()).append('\n');
        export.append("Budget: $").append(trip.budget()).append('\n');
        export.append("Days: ").append(trip.days()).append('\n');
        export.append("Favorite: ").append(trip.favorite()).append('\n');
        export.append("User Message: ").append(trip.userMessage()).append("\n\n");

        appendSection(export, "Tags");
        for (TripTagResponse tag : tags) {
            export.append("- ").append(tag.name()).append('\n');
        }
        appendEmptyLineForEmpty(export, tags);

        appendSection(export, "Notes");
        for (TripNoteResponse note : notes) {
            export.append("- ").append(note.content()).append('\n');
        }
        appendEmptyLineForEmpty(export, notes);

        appendSection(export, "Checklist");
        for (TripChecklistItemResponse item : checklist) {
            export.append("- [").append(item.completed() ? "x" : " ").append("] ")
                    .append(item.title()).append('\n');
        }
        appendEmptyLineForEmpty(export, checklist);

        appendSection(export, "Documents");
        for (TripDocumentResponse document : documents) {
            export.append("- ").append(document.name())
                    .append(" (").append(document.type()).append("): ")
                    .append(document.url()).append('\n');
        }
        appendEmptyLineForEmpty(export, documents);

        appendSection(export, "Budget Items");
        for (TripBudgetItemResponse item : budgetItems) {
            export.append("- ").append(item.title())
                    .append(" | ").append(item.category())
                    .append(" | $").append(item.amount()).append('\n');
        }
        appendEmptyLineForEmpty(export, budgetItems);

        appendSection(export, "Itinerary");
        for (TripItineraryItemResponse item : itinerary) {
            export.append("- Day ").append(item.dayNumber())
                    .append(" | ").append(item.startTime()).append("-").append(item.endTime())
                    .append(" | ").append(item.title())
                    .append(" @ ").append(item.location())
                    .append(" | ").append(item.description())
                    .append('\n');
        }
        appendEmptyLineForEmpty(export, itinerary);

        return export.toString();
    }

    private void appendSection(StringBuilder builder, String title) {
        builder.append(title).append('\n');
        builder.append("-".repeat(title.length())).append('\n');
    }

    private void appendEmptyLineForEmpty(StringBuilder builder, List<?> items) {
        if (items.isEmpty()) {
            builder.append("- None\n");
        }
        builder.append('\n');
    }

    private String defaultString(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private LocalTime defaultTime(LocalTime value, LocalTime defaultValue) {
        return value == null ? defaultValue : value;
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
