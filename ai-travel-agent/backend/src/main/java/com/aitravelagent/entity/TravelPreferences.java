package com.aitravelagent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "travel_preferences")
public class TravelPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int preferredBudget;

    @Column(nullable = false)
    private int preferredDuration;

    @Column(nullable = false)
    private String preferredTravelStyle;

    @Column(nullable = false)
    private String preferredDestination;

    public Long getId() {
        return id;
    }

    public int getPreferredBudget() {
        return preferredBudget;
    }

    public void setPreferredBudget(int preferredBudget) {
        this.preferredBudget = preferredBudget;
    }

    public int getPreferredDuration() {
        return preferredDuration;
    }

    public void setPreferredDuration(int preferredDuration) {
        this.preferredDuration = preferredDuration;
    }

    public String getPreferredTravelStyle() {
        return preferredTravelStyle;
    }

    public void setPreferredTravelStyle(String preferredTravelStyle) {
        this.preferredTravelStyle = preferredTravelStyle;
    }

    public String getPreferredDestination() {
        return preferredDestination;
    }

    public void setPreferredDestination(String preferredDestination) {
        this.preferredDestination = preferredDestination;
    }
}
