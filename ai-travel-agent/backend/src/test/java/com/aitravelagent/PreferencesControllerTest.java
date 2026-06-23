package com.aitravelagent;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PreferencesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM travel_preferences");
    }

    @Test
    void getPreferencesReturnsDefaultsWhenNoneAreSaved() throws Exception {
        mockMvc.perform(get("/api/preferences"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.preferredBudget").value(1500))
                .andExpect(jsonPath("$.preferredDuration").value(7))
                .andExpect(jsonPath("$.preferredTravelStyle").value("Relaxed"))
                .andExpect(jsonPath("$.preferredDestination").value("Dubai"));
    }

    @Test
    void putPreferencesSavesAndReturnsPreferences() throws Exception {
        mockMvc.perform(put("/api/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "preferredBudget": 2400,
                                  "preferredDuration": 10,
                                  "preferredTravelStyle": "Adventure",
                                  "preferredDestination": "Tokyo"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredBudget").value(2400))
                .andExpect(jsonPath("$.preferredDuration").value(10))
                .andExpect(jsonPath("$.preferredTravelStyle").value("Adventure"))
                .andExpect(jsonPath("$.preferredDestination").value("Tokyo"));

        mockMvc.perform(get("/api/preferences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredBudget").value(2400))
                .andExpect(jsonPath("$.preferredDuration").value(10))
                .andExpect(jsonPath("$.preferredTravelStyle").value("Adventure"))
                .andExpect(jsonPath("$.preferredDestination").value("Tokyo"));
    }

    @Test
    void putPreferencesUsesDefaultsForInvalidValues() throws Exception {
        mockMvc.perform(put("/api/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "preferredBudget": 0,
                                  "preferredDuration": -2,
                                  "preferredTravelStyle": " ",
                                  "preferredDestination": ""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredBudget").value(1500))
                .andExpect(jsonPath("$.preferredDuration").value(7))
                .andExpect(jsonPath("$.preferredTravelStyle").value("Relaxed"))
                .andExpect(jsonPath("$.preferredDestination").value("Dubai"));
    }
}
