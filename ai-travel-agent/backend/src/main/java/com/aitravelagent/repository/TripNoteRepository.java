package com.aitravelagent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aitravelagent.entity.TripNote;

public interface TripNoteRepository extends JpaRepository<TripNote, Long> {

    List<TripNote> findByTripIdOrderByCreatedAtDesc(Long tripId);

    void deleteByTripId(Long tripId);
}
