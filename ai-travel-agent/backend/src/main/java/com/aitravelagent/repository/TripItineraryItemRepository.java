package com.aitravelagent.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aitravelagent.entity.TripItineraryItem;

public interface TripItineraryItemRepository extends JpaRepository<TripItineraryItem, Long> {

    List<TripItineraryItem> findByTripIdOrderByDayNumberAscStartTimeAsc(Long tripId);

    Optional<TripItineraryItem> findByIdAndTrip_Id(Long id, Long tripId);

    void deleteByTripId(Long tripId);
}
