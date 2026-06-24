package com.aitravelagent;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM trip_notes");
        jdbcTemplate.update("DELETE FROM travel_preferences");
        jdbcTemplate.update("DELETE FROM saved_trips");
    }

    @Test
    void planTripReturnsMockTripPlan() throws Exception {
        mockMvc.perform(post("/api/trips/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Plan a 7-day trip from Austin to Dubai under $1500"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.destination").value("Dubai"))
                .andExpect(jsonPath("$.origin").value("Austin"))
                .andExpect(jsonPath("$.budget").value(1500))
                .andExpect(jsonPath("$.days").value(7))
                .andExpect(jsonPath("$.flightOptions[0].airline").value("Mock Airline"))
                .andExpect(jsonPath("$.flightOptions[0].price").value(850))
                .andExpect(jsonPath("$.flightOptions[0].duration").value("18h 30m"))
                .andExpect(jsonPath("$.hotelOptions[0].name").value("Mock Hotel Dubai"))
                .andExpect(jsonPath("$.hotelOptions[0].pricePerNight").value(90))
                .andExpect(jsonPath("$.hotelOptions[0].rating").value(4.3))
                .andExpect(jsonPath("$.itinerary[0].day").value(1))
                .andExpect(jsonPath("$.itinerary[0].title").value("Arrival and hotel check-in"))
                .andExpect(jsonPath("$.itinerary[0].activities[0]").value("Arrive in Dubai"))
                .andExpect(jsonPath("$.itinerary[0].activities[1]").value("Check into hotel"))
                .andExpect(jsonPath("$.itinerary[0].activities[2]").value("Evening walk"));
    }

    @Test
    void planTripParsesTripDetailsFromMessage() throws Exception {
        mockMvc.perform(post("/api/trips/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Plan a 3-day trip from Kabul to Tokyo under $2500"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destination").value("Tokyo"))
                .andExpect(jsonPath("$.origin").value("Kabul"))
                .andExpect(jsonPath("$.budget").value(2500))
                .andExpect(jsonPath("$.days").value(3))
                .andExpect(jsonPath("$.hotelOptions[0].name").value("Mock Hotel Tokyo"))
                .andExpect(jsonPath("$.itinerary[0].activities[0]").value("Arrive in Tokyo"));
    }

    @Test
    void planTripUsesDefaultsWhenMessageIsMissingDetails() throws Exception {
        mockMvc.perform(post("/api/trips/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Please plan something fun"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destination").value("Dubai"))
                .andExpect(jsonPath("$.origin").value("Austin"))
                .andExpect(jsonPath("$.budget").value(1500))
                .andExpect(jsonPath("$.days").value(7));
    }

    @Test
    void apiCorsAllowsConfiguredFrontendOrigins() throws Exception {
        String[] origins = {
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:5175",
                "http://127.0.0.1:5175"
        };

        for (String origin : origins) {
            mockMvc.perform(get("/api/trips")
                            .header(HttpHeaders.ORIGIN, origin))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin));
        }
    }

    @Test
    void apiCorsAllowsConfiguredMethodsAndHeaders() throws Exception {
        mockMvc.perform(options("/api/trips/plan")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5175")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.POST.name())
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5175"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "content-type"))
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        "GET,POST,PUT,PATCH,DELETE,OPTIONS"
                ));
    }

    @Test
    void saveTripStoresBasicTripData() throws Exception {
        mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Plan a 5-day trip from Austin to Paris under $2200",
                                  "origin": "Austin",
                                  "destination": "Paris",
                                  "budget": 2200,
                                  "days": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userMessage").value("Plan a 5-day trip from Austin to Paris under $2200"))
                .andExpect(jsonPath("$.origin").value("Austin"))
                .andExpect(jsonPath("$.destination").value("Paris"))
                .andExpect(jsonPath("$.budget").value(2200))
                .andExpect(jsonPath("$.days").value(5))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void updateTripChangesSavedTripFields() throws Exception {
        MvcResult saveResult = mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Original trip",
                                  "origin": "Austin",
                                  "destination": "Rome",
                                  "budget": 1500,
                                  "days": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Integer id = JsonPath.read(saveResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(put("/api/trips/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Updated Rome trip",
                                  "origin": "Dallas",
                                  "destination": "Florence",
                                  "budget": 2100,
                                  "days": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.userMessage").value("Updated Rome trip"))
                .andExpect(jsonPath("$.origin").value("Dallas"))
                .andExpect(jsonPath("$.destination").value("Florence"))
                .andExpect(jsonPath("$.budget").value(2100))
                .andExpect(jsonPath("$.days").value(5))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void updateTripReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(put("/api/trips/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Missing trip",
                                  "origin": "Austin",
                                  "destination": "Rome",
                                  "budget": 1500,
                                  "days": 3
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTripsReturnsSavedTrips() throws Exception {
        mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Plan a 2-day trip from Austin to Miami under $800",
                                  "origin": "Austin",
                                  "destination": "Miami",
                                  "budget": 800,
                                  "days": 2
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].origin").value("Austin"))
                .andExpect(jsonPath("$[0].destination").value("Miami"))
                .andExpect(jsonPath("$[0].budget").value(800))
                .andExpect(jsonPath("$[0].days").value(2))
                .andExpect(jsonPath("$[0].favorite").value(false))
                .andExpect(jsonPath("$[0].updatedAt").isNotEmpty());
    }

    @Test
    void getRecentTripsReturnsFiveMostRecentlyCreatedTrips() throws Exception {
        for (int index = 1; index <= 6; index++) {
            insertSavedTrip(
                    "Trip " + index,
                    "Origin " + index,
                    "Destination " + index,
                    Instant.parse("2026-01-0" + index + "T00:00:00Z")
            );
        }

        mockMvc.perform(get("/api/trips/recent"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].destination").value("Destination 6"))
                .andExpect(jsonPath("$[1].destination").value("Destination 5"))
                .andExpect(jsonPath("$[2].destination").value("Destination 4"))
                .andExpect(jsonPath("$[3].destination").value("Destination 3"))
                .andExpect(jsonPath("$[4].destination").value("Destination 2"));
    }

    @Test
    void getTripStatsReturnsSavedTripSummary() throws Exception {
        insertSavedTrip(
                "First Dubai plan",
                "Austin",
                "Dubai",
                1000,
                false,
                Instant.parse("2026-01-01T00:00:00Z")
        );
        insertSavedTrip(
                "Second Dubai plan",
                "Dallas",
                "Dubai",
                2000,
                true,
                Instant.parse("2026-01-02T00:00:00Z")
        );
        insertSavedTrip(
                "Tokyo plan",
                "Seattle",
                "Tokyo",
                3000,
                true,
                Instant.parse("2026-01-03T00:00:00Z")
        );

        mockMvc.perform(get("/api/trips/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTrips").value(3))
                .andExpect(jsonPath("$.favoriteTrips").value(2))
                .andExpect(jsonPath("$.averageBudget").value(2000.0))
                .andExpect(jsonPath("$.mostCommonDestination").value("Dubai"));
    }

    @Test
    void getTripStatsReturnsDefaultsWhenNoTripsExist() throws Exception {
        mockMvc.perform(get("/api/trips/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTrips").value(0))
                .andExpect(jsonPath("$.favoriteTrips").value(0))
                .andExpect(jsonPath("$.averageBudget").value(0.0))
                .andExpect(jsonPath("$.mostCommonDestination").value(""));
    }

    @Test
    void getRecommendationsReturnsThreeMockTrips() throws Exception {
        mockMvc.perform(get("/api/trips/recommendations"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.recommendations.length()").value(3))
                .andExpect(jsonPath("$.recommendations[0].origin").value("Austin"))
                .andExpect(jsonPath("$.recommendations[0].destination").value("Dubai"))
                .andExpect(jsonPath("$.recommendations[0].budget").value(1500))
                .andExpect(jsonPath("$.recommendations[0].days").value(7))
                .andExpect(jsonPath("$.recommendations[0].travelStyle").value("Relaxed"))
                .andExpect(jsonPath("$.recommendations[1].destination").value("Bali"))
                .andExpect(jsonPath("$.recommendations[1].travelStyle").value("Relaxed"))
                .andExpect(jsonPath("$.recommendations[2].destination").value("Tokyo"))
                .andExpect(jsonPath("$.recommendations[2].travelStyle").value("Relaxed"));
    }

    @Test
    void getRecommendationsUsesPreferencesAndSavedTrips() throws Exception {
        mockMvc.perform(put("/api/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "preferredBudget": 2200,
                                  "preferredDuration": 6,
                                  "preferredTravelStyle": "Adventure",
                                  "preferredDestination": "Lisbon"
                                }
                                """))
                .andExpect(status().isOk());
        insertSavedTrip(
                "Recent Tokyo trip",
                "Austin",
                "Tokyo",
                1800,
                false,
                Instant.parse("2026-01-03T00:00:00Z")
        );
        insertSavedTrip(
                "Older Tokyo trip",
                "Dallas",
                "Tokyo",
                1900,
                false,
                Instant.parse("2026-01-02T00:00:00Z")
        );

        mockMvc.perform(get("/api/trips/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations.length()").value(3))
                .andExpect(jsonPath("$.recommendations[0].origin").value("Austin"))
                .andExpect(jsonPath("$.recommendations[0].destination").value("Lisbon"))
                .andExpect(jsonPath("$.recommendations[0].budget").value(2200))
                .andExpect(jsonPath("$.recommendations[0].days").value(6))
                .andExpect(jsonPath("$.recommendations[0].travelStyle").value("Adventure"))
                .andExpect(jsonPath("$.recommendations[1].destination").value("Tokyo"))
                .andExpect(jsonPath("$.recommendations[2].destination").value("Cape Town"));
    }

    @Test
    void postRecommendationsIsNotSupported() throws Exception {
        mockMvc.perform(post("/api/trips/recommendations"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void getTripsHandlesLegacyRowsWithNullFavoriteAndUpdatedAt() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO saved_trips (
                  user_message,
                  origin,
                  destination,
                  budget,
                  days,
                  favorite,
                  created_at,
                  updated_at
                )
                VALUES (
                  'LegacyNull beach plan',
                  'Austin',
                  'Dubai',
                  1500,
                  7,
                  NULL,
                  TIMESTAMP '2000-01-01 00:00:00',
                  NULL
                )
                """);

        mockMvc.perform(get("/api/trips/search")
                        .param("q", "LegacyNull"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].origin").value("Austin"))
                .andExpect(jsonPath("$[0].destination").value("Dubai"))
                .andExpect(jsonPath("$[0].favorite").value(false))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[0].updatedAt").isNotEmpty());
    }

    @Test
    void getTripReturnsSavedTripById() throws Exception {
        MvcResult saveResult = mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Plan a 6-day trip from Austin to Lisbon under $1800",
                                  "origin": "Austin",
                                  "destination": "Lisbon",
                                  "budget": 1800,
                                  "days": 6
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Integer id = JsonPath.read(saveResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/trips/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.userMessage").value("Plan a 6-day trip from Austin to Lisbon under $1800"))
                .andExpect(jsonPath("$.origin").value("Austin"))
                .andExpect(jsonPath("$.destination").value("Lisbon"))
                .andExpect(jsonPath("$.budget").value(1800))
                .andExpect(jsonPath("$.days").value(6))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void getTripReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(get("/api/trips/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTripNoteCreatesNoteForSavedTrip() throws Exception {
        MvcResult saveResult = mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Plan a 5-day trip from Austin to Dubai under $1500",
                                  "origin": "Austin",
                                  "destination": "Dubai",
                                  "budget": 1500,
                                  "days": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Integer id = JsonPath.read(saveResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/trips/{id}/notes", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Ask for a hotel near the metro."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tripId").value(id))
                .andExpect(jsonPath("$.content").value("Ask for a hotel near the metro."))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void addTripNoteReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(post("/api/trips/{id}/notes", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "This note should not be saved."
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTripNotesReturnsNotesNewestFirst() throws Exception {
        MvcResult saveResult = mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Plan a 7-day trip from Austin to Dubai under $1500",
                                  "origin": "Austin",
                                  "destination": "Dubai",
                                  "budget": 1500,
                                  "days": 7
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Integer id = JsonPath.read(saveResult.getResponse().getContentAsString(), "$.id");
        insertTripNote(id, "Older note", Instant.parse("2026-01-01T00:00:00Z"));
        insertTripNote(id, "Newer note", Instant.parse("2026-01-02T00:00:00Z"));

        mockMvc.perform(get("/api/trips/{id}/notes", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tripId").value(id))
                .andExpect(jsonPath("$[0].content").value("Newer note"))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[1].tripId").value(id))
                .andExpect(jsonPath("$[1].content").value("Older note"));
    }

    @Test
    void getTripNotesReturnsEmptyListForTripWithoutNotes() throws Exception {
        MvcResult saveResult = mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Plan a weekend trip",
                                  "origin": "Austin",
                                  "destination": "Denver",
                                  "budget": 900,
                                  "days": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Integer id = JsonPath.read(saveResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/trips/{id}/notes", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTripNotesReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(get("/api/trips/{id}/notes", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTripNoteRemovesNoteFromSavedTrip() throws Exception {
        MvcResult saveResult = mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Plan a 5-day trip from Austin to Dubai under $1500",
                                  "origin": "Austin",
                                  "destination": "Dubai",
                                  "budget": 1500,
                                  "days": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Integer tripId = JsonPath.read(saveResult.getResponse().getContentAsString(), "$.id");
        MvcResult noteResult = mockMvc.perform(post("/api/trips/{id}/notes", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Delete this note later."
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Integer noteId = JsonPath.read(noteResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/api/trips/{tripId}/notes/{noteId}", tripId, noteId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/trips/{id}/notes", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteTripNoteReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(delete("/api/trips/{tripId}/notes/{noteId}", 999999L, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTripNoteReturnsNotFoundForMissingNote() throws Exception {
        MvcResult saveResult = mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Plan a weekend trip",
                                  "origin": "Austin",
                                  "destination": "Denver",
                                  "budget": 900,
                                  "days": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Integer tripId = JsonPath.read(saveResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/api/trips/{tripId}/notes/{noteId}", tripId, 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTripNoteReturnsNotFoundWhenNoteBelongsToAnotherTrip() throws Exception {
        MvcResult firstTripResult = mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "First trip",
                                  "origin": "Austin",
                                  "destination": "Dubai",
                                  "budget": 1500,
                                  "days": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult secondTripResult = mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Second trip",
                                  "origin": "Austin",
                                  "destination": "Lisbon",
                                  "budget": 1800,
                                  "days": 6
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Integer firstTripId = JsonPath.read(firstTripResult.getResponse().getContentAsString(), "$.id");
        Integer secondTripId = JsonPath.read(secondTripResult.getResponse().getContentAsString(), "$.id");
        MvcResult noteResult = mockMvc.perform(post("/api/trips/{id}/notes", firstTripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "This note belongs to the first trip."
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Integer noteId = JsonPath.read(noteResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/api/trips/{tripId}/notes/{noteId}", secondTripId, noteId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/trips/{id}/notes", firstTripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(noteId));
    }

    @Test
    void deleteTripRemovesSavedTripWithNotes() throws Exception {
        MvcResult saveResult = mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Plan a 4-day trip from Austin to Denver under $900",
                                  "origin": "Austin",
                                  "destination": "Denver",
                                  "budget": 900,
                                  "days": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Integer id = JsonPath.read(saveResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/trips/{id}/notes", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Check hotel cancellation policy."
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/trips/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void toggleFavoriteUpdatesFavoriteStatus() throws Exception {
        MvcResult saveResult = mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Plan a 5-day trip from Austin to Seoul under $2000",
                                  "origin": "Austin",
                                  "destination": "Seoul",
                                  "budget": 2000,
                                  "days": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite").value(false))
                .andReturn();

        Integer id = JsonPath.read(saveResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(patch("/api/trips/{id}/favorite", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.favorite").value(true));

        mockMvc.perform(get("/api/trips")
                        .param("favorite", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].favorite").value(true));
    }

    @Test
    void toggleFavoriteReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(patch("/api/trips/{id}/favorite", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateTripCreatesCopyWithNewId() throws Exception {
        MvcResult saveResult = mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Plan a 3-day trip from Austin to Porto under $1400",
                                  "origin": "Austin",
                                  "destination": "Porto",
                                  "budget": 1400,
                                  "days": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Integer originalId = JsonPath.read(saveResult.getResponse().getContentAsString(), "$.id");

        MvcResult duplicateResult = mockMvc.perform(post("/api/trips/{id}/duplicate", originalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origin").value("Austin"))
                .andExpect(jsonPath("$.destination").value("Porto"))
                .andExpect(jsonPath("$.budget").value(1400))
                .andExpect(jsonPath("$.days").value(3))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andReturn();

        Integer duplicateId = JsonPath.read(duplicateResult.getResponse().getContentAsString(), "$.id");
        assertNotEquals(originalId, duplicateId);
    }

    @Test
    void duplicateTripReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(post("/api/trips/{id}/duplicate", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchTripsMatchesDestinationOriginAndUserMessage() throws Exception {
        mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Relaxing beach plan",
                                  "origin": "Austin",
                                  "destination": "Dubai",
                                  "budget": 2300,
                                  "days": 6
                                }
                                """))
                .andExpect(status().isOk());

        for (String query : new String[] { "Dubai", "Austin", "beach" }) {
            mockMvc.perform(get("/api/trips/search")
                            .param("q", query))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].origin").value("Austin"))
                    .andExpect(jsonPath("$[0].destination").value("Dubai"))
                    .andExpect(jsonPath("$[0].userMessage").value("Relaxing beach plan"));
        }
    }

    @Test
    void deleteTripRemovesSavedTrip() throws Exception {
        MvcResult saveResult = mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "Plan a 4-day trip from Austin to Denver under $900",
                                  "origin": "Austin",
                                  "destination": "Denver",
                                  "budget": 900,
                                  "days": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Integer id = JsonPath.read(saveResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/api/trips/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(delete("/api/trips/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTripReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(delete("/api/trips/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    private void insertSavedTrip(String userMessage, String origin, String destination, Instant createdAt) {
        insertSavedTrip(userMessage, origin, destination, 1500, false, createdAt);
    }

    private void insertSavedTrip(
            String userMessage,
            String origin,
            String destination,
            int budget,
            boolean favorite,
            Instant createdAt
    ) {
        jdbcTemplate.update("""
                INSERT INTO saved_trips (
                  user_message,
                  origin,
                  destination,
                  budget,
                  days,
                  favorite,
                  created_at,
                  updated_at
                )
                VALUES (?, ?, ?, ?, 7, ?, ?, ?)
                """,
                userMessage,
                origin,
                destination,
                budget,
                favorite,
                Timestamp.from(createdAt),
                Timestamp.from(createdAt)
        );
    }

    private void insertTripNote(Integer tripId, String content, Instant createdAt) {
        jdbcTemplate.update("""
                INSERT INTO trip_notes (
                  trip_id,
                  content,
                  created_at
                )
                VALUES (?, ?, ?)
                """,
                tripId,
                content,
                Timestamp.from(createdAt)
        );
    }
}
