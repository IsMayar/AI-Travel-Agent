package com.aitravelagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aitravelagent.dto.TravelPreferencesRequest;
import com.aitravelagent.dto.TravelPreferencesResponse;
import com.aitravelagent.service.TravelPreferencesService;

@RestController
@RequestMapping("/api/preferences")
public class PreferencesController {

    private final TravelPreferencesService travelPreferencesService;

    public PreferencesController(TravelPreferencesService travelPreferencesService) {
        this.travelPreferencesService = travelPreferencesService;
    }

    @GetMapping
    public TravelPreferencesResponse getPreferences() {
        return travelPreferencesService.getPreferences();
    }

    @PutMapping
    public TravelPreferencesResponse updatePreferences(@RequestBody TravelPreferencesRequest request) {
        return travelPreferencesService.updatePreferences(request);
    }
}
