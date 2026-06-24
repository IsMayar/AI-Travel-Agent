package com.aitravelagent.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aitravelagent.entity.TripChecklistItem;

public interface TripChecklistItemRepository extends JpaRepository<TripChecklistItem, Long> {

    List<TripChecklistItem> findByTripIdOrderByCreatedAtAsc(Long tripId);

    Optional<TripChecklistItem> findByIdAndTrip_Id(Long id, Long tripId);

    void deleteByTripId(Long tripId);
}
