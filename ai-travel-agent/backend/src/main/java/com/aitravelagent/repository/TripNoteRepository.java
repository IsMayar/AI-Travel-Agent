package com.aitravelagent.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aitravelagent.entity.TripNote;

public interface TripNoteRepository extends JpaRepository<TripNote, Long> {

    void deleteByTripId(Long tripId);
}
