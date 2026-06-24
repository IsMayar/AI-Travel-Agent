package com.aitravelagent.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aitravelagent.entity.TripTag;

public interface TripTagRepository extends JpaRepository<TripTag, Long> {

    List<TripTag> findByTripIdOrderByNameAsc(Long tripId);

    Optional<TripTag> findByIdAndTrip_Id(Long id, Long tripId);

    Optional<TripTag> findFirstByTrip_IdAndNameIgnoreCase(Long tripId, String name);

    boolean existsByTrip_IdAndNameIgnoreCase(Long tripId, String name);

    void deleteByTripId(Long tripId);
}
