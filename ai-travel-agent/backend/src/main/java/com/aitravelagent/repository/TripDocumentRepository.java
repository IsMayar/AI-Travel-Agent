package com.aitravelagent.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aitravelagent.entity.TripDocument;

public interface TripDocumentRepository extends JpaRepository<TripDocument, Long> {

    List<TripDocument> findByTripIdOrderByCreatedAtDesc(Long tripId);

    Optional<TripDocument> findByIdAndTrip_Id(Long id, Long tripId);

    void deleteByTripId(Long tripId);
}
