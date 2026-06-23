package com.aitravelagent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aitravelagent.entity.SavedTrip;

public interface SavedTripRepository extends JpaRepository<SavedTrip, Long> {

    List<SavedTrip> findAllByOrderByCreatedAtDesc();

    List<SavedTrip> findTop5ByOrderByCreatedAtDesc();

    @Query("""
            SELECT trip
            FROM SavedTrip trip
            WHERE (:favorite = true AND trip.favorite = true)
               OR (:favorite = false AND (trip.favorite = false OR trip.favorite IS NULL))
            ORDER BY trip.createdAt DESC
            """)
    List<SavedTrip> findAllByFavoriteStatusOrderByCreatedAtDesc(@Param("favorite") boolean favorite);

    @Query("""
            SELECT trip
            FROM SavedTrip trip
            WHERE LOWER(COALESCE(trip.destination, '')) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(COALESCE(trip.origin, '')) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(COALESCE(trip.userMessage, '')) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY trip.createdAt DESC
            """)
    List<SavedTrip> searchTrips(@Param("query") String query);
}
