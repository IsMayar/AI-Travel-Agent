package com.aitravelagent.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aitravelagent.entity.TripBudgetItem;

public interface TripBudgetItemRepository extends JpaRepository<TripBudgetItem, Long> {

    List<TripBudgetItem> findByTripIdOrderByCreatedAtAsc(Long tripId);

    Optional<TripBudgetItem> findByIdAndTrip_Id(Long id, Long tripId);

    void deleteByTripId(Long tripId);
}
