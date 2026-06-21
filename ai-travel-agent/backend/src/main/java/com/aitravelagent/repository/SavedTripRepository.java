package com.aitravelagent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aitravelagent.entity.SavedTrip;

public interface SavedTripRepository extends JpaRepository<SavedTrip, Long> {

    List<SavedTrip> findAllByOrderByCreatedAtDesc();
}
