package com.aitravelagent.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aitravelagent.entity.TravelPreferences;

public interface TravelPreferencesRepository extends JpaRepository<TravelPreferences, Long> {

    Optional<TravelPreferences> findTopByOrderByIdAsc();
}
