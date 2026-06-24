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
        jdbcTemplate.update("DELETE FROM trip_tags");
        jdbcTemplate.update("DELETE FROM trip_itinerary_items");
        jdbcTemplate.update("DELETE FROM trip_budget_items");
        jdbcTemplate.update("DELETE FROM trip_documents");
        jdbcTemplate.update("DELETE FROM trip_checklist_items");
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
    void updateTripNoteChangesNoteContent() throws Exception {
        Integer tripId = saveBasicTrip("Plan a Dubai trip", "Dubai");
        Integer noteId = createTripNote(tripId, "Original note");

        mockMvc.perform(put("/api/trips/{tripId}/notes/{noteId}", tripId, noteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Updated note content."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(noteId))
                .andExpect(jsonPath("$.tripId").value(tripId))
                .andExpect(jsonPath("$.content").value("Updated note content."))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void updateTripNoteReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(put("/api/trips/{tripId}/notes/{noteId}", 999999L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Missing trip update."
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTripNoteReturnsNotFoundForMissingNote() throws Exception {
        Integer tripId = saveBasicTrip("Plan a Denver trip", "Denver");

        mockMvc.perform(put("/api/trips/{tripId}/notes/{noteId}", tripId, 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Missing note update."
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTripNoteReturnsNotFoundWhenNoteBelongsToAnotherTrip() throws Exception {
        Integer firstTripId = saveBasicTrip("First note trip", "Dubai");
        Integer secondTripId = saveBasicTrip("Second note trip", "Lisbon");
        Integer noteId = createTripNote(firstTripId, "First trip note");

        mockMvc.perform(put("/api/trips/{tripId}/notes/{noteId}", secondTripId, noteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Wrong trip update."
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/trips/{id}/notes", firstTripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("First trip note"));
    }

    @Test
    void getTripChecklistReturnsItemsOldestFirst() throws Exception {
        Integer tripId = saveBasicTrip("Checklist trip", "Dubai");
        insertChecklistItem(tripId, "Book hotel", false, Instant.parse("2026-01-01T00:00:00Z"));
        insertChecklistItem(tripId, "Pack passport", true, Instant.parse("2026-01-02T00:00:00Z"));

        mockMvc.perform(get("/api/trips/{tripId}/checklist", tripId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tripId").value(tripId))
                .andExpect(jsonPath("$[0].title").value("Book hotel"))
                .andExpect(jsonPath("$[0].completed").value(false))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[1].title").value("Pack passport"))
                .andExpect(jsonPath("$[1].completed").value(true));
    }

    @Test
    void getTripChecklistReturnsEmptyListForTripWithoutItems() throws Exception {
        Integer tripId = saveBasicTrip("Empty checklist trip", "Denver");

        mockMvc.perform(get("/api/trips/{tripId}/checklist", tripId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTripChecklistReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(get("/api/trips/{tripId}/checklist", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTripChecklistItemCreatesIncompleteItem() throws Exception {
        Integer tripId = saveBasicTrip("Checklist create trip", "Tokyo");

        mockMvc.perform(post("/api/trips/{tripId}/checklist", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Check visa requirements"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tripId").value(tripId))
                .andExpect(jsonPath("$.title").value("Check visa requirements"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void addTripChecklistItemReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(post("/api/trips/{tripId}/checklist", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "This should not be saved"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void toggleTripChecklistItemUpdatesCompletedStatus() throws Exception {
        Integer tripId = saveBasicTrip("Checklist toggle trip", "Rome");
        Integer itemId = createChecklistItem(tripId, "Pack charger");

        mockMvc.perform(patch("/api/trips/{tripId}/checklist/{itemId}", tripId, itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.tripId").value(tripId))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void toggleTripChecklistItemReturnsNotFoundForMissingItem() throws Exception {
        Integer tripId = saveBasicTrip("Checklist missing item trip", "Seoul");

        mockMvc.perform(patch("/api/trips/{tripId}/checklist/{itemId}", tripId, 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void toggleTripChecklistItemReturnsNotFoundWhenItemBelongsToAnotherTrip() throws Exception {
        Integer firstTripId = saveBasicTrip("First checklist trip", "Dubai");
        Integer secondTripId = saveBasicTrip("Second checklist trip", "Lisbon");
        Integer itemId = createChecklistItem(firstTripId, "Confirm airport transfer");

        mockMvc.perform(patch("/api/trips/{tripId}/checklist/{itemId}", secondTripId, itemId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTripChecklistItemRemovesItem() throws Exception {
        Integer tripId = saveBasicTrip("Checklist delete trip", "Porto");
        Integer itemId = createChecklistItem(tripId, "Download offline maps");

        mockMvc.perform(delete("/api/trips/{tripId}/checklist/{itemId}", tripId, itemId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/trips/{tripId}/checklist", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteTripChecklistItemReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(delete("/api/trips/{tripId}/checklist/{itemId}", 999999L, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTripChecklistItemReturnsNotFoundWhenItemBelongsToAnotherTrip() throws Exception {
        Integer firstTripId = saveBasicTrip("First checklist delete trip", "Dubai");
        Integer secondTripId = saveBasicTrip("Second checklist delete trip", "Tokyo");
        Integer itemId = createChecklistItem(firstTripId, "Keep this item");

        mockMvc.perform(delete("/api/trips/{tripId}/checklist/{itemId}", secondTripId, itemId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/trips/{tripId}/checklist", firstTripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(itemId));
    }

    @Test
    void getTripDocumentsReturnsDocumentsNewestFirst() throws Exception {
        Integer tripId = saveBasicTrip("Document trip", "Dubai");
        insertTripDocument(
                tripId,
                "Older passport scan",
                "Passport",
                "https://example.com/passport-old",
                Instant.parse("2026-01-01T00:00:00Z")
        );
        insertTripDocument(
                tripId,
                "Newer hotel voucher",
                "Voucher",
                "https://example.com/hotel",
                Instant.parse("2026-01-02T00:00:00Z")
        );

        mockMvc.perform(get("/api/trips/{tripId}/documents", tripId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tripId").value(tripId))
                .andExpect(jsonPath("$[0].name").value("Newer hotel voucher"))
                .andExpect(jsonPath("$[0].type").value("Voucher"))
                .andExpect(jsonPath("$[0].url").value("https://example.com/hotel"))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[1].name").value("Older passport scan"));
    }

    @Test
    void getTripDocumentsReturnsEmptyListForTripWithoutDocuments() throws Exception {
        Integer tripId = saveBasicTrip("Empty document trip", "Denver");

        mockMvc.perform(get("/api/trips/{tripId}/documents", tripId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTripDocumentsReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(get("/api/trips/{tripId}/documents", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTripDocumentCreatesMetadataOnlyDocument() throws Exception {
        Integer tripId = saveBasicTrip("Create document trip", "Tokyo");

        mockMvc.perform(post("/api/trips/{tripId}/documents", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Passport scan",
                                  "type": "Passport",
                                  "url": "https://example.com/passport"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tripId").value(tripId))
                .andExpect(jsonPath("$.name").value("Passport scan"))
                .andExpect(jsonPath("$.type").value("Passport"))
                .andExpect(jsonPath("$.url").value("https://example.com/passport"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void addTripDocumentReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(post("/api/trips/{tripId}/documents", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Missing document",
                                  "type": "Link",
                                  "url": "https://example.com/missing"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTripDocumentRemovesDocument() throws Exception {
        Integer tripId = saveBasicTrip("Delete document trip", "Porto");
        Integer documentId = createTripDocument(tripId, "Insurance PDF", "Insurance", "https://example.com/insurance");

        mockMvc.perform(delete("/api/trips/{tripId}/documents/{documentId}", tripId, documentId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/trips/{tripId}/documents", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteTripDocumentReturnsNotFoundWhenDocumentBelongsToAnotherTrip() throws Exception {
        Integer firstTripId = saveBasicTrip("First document trip", "Dubai");
        Integer secondTripId = saveBasicTrip("Second document trip", "Lisbon");
        Integer documentId = createTripDocument(firstTripId, "Visa link", "Visa", "https://example.com/visa");

        mockMvc.perform(delete("/api/trips/{tripId}/documents/{documentId}", secondTripId, documentId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/trips/{tripId}/documents", firstTripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(documentId));
    }

    @Test
    void getTripBudgetItemsReturnsItemsOldestFirst() throws Exception {
        Integer tripId = saveBasicTrip("Budget trip", "Dubai");
        insertTripBudgetItem(tripId, "Flight", "Transport", 850.00, Instant.parse("2026-01-01T00:00:00Z"));
        insertTripBudgetItem(tripId, "Hotel", "Lodging", 450.50, Instant.parse("2026-01-02T00:00:00Z"));

        mockMvc.perform(get("/api/trips/{tripId}/budget-items", tripId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tripId").value(tripId))
                .andExpect(jsonPath("$[0].title").value("Flight"))
                .andExpect(jsonPath("$[0].category").value("Transport"))
                .andExpect(jsonPath("$[0].amount").value(850.00))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[0].updatedAt").isNotEmpty())
                .andExpect(jsonPath("$[1].title").value("Hotel"))
                .andExpect(jsonPath("$[1].amount").value(450.50));
    }

    @Test
    void getTripBudgetItemsReturnsEmptyListForTripWithoutItems() throws Exception {
        Integer tripId = saveBasicTrip("Empty budget trip", "Denver");

        mockMvc.perform(get("/api/trips/{tripId}/budget-items", tripId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTripBudgetItemsReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(get("/api/trips/{tripId}/budget-items", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTripBudgetItemCreatesItem() throws Exception {
        Integer tripId = saveBasicTrip("Create budget trip", "Tokyo");

        mockMvc.perform(post("/api/trips/{tripId}/budget-items", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Museum tickets",
                                  "category": "Activities",
                                  "amount": 120.75
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tripId").value(tripId))
                .andExpect(jsonPath("$.title").value("Museum tickets"))
                .andExpect(jsonPath("$.category").value("Activities"))
                .andExpect(jsonPath("$.amount").value(120.75))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void addTripBudgetItemReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(post("/api/trips/{tripId}/budget-items", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Missing budget",
                                  "category": "General",
                                  "amount": 25.00
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTripBudgetItemChangesFields() throws Exception {
        Integer tripId = saveBasicTrip("Update budget trip", "Rome");
        Integer itemId = createTripBudgetItem(tripId, "Taxi", "Transport", 45.00);

        mockMvc.perform(put("/api/trips/{tripId}/budget-items/{itemId}", tripId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Airport taxi",
                                  "category": "Ground transport",
                                  "amount": 65.25
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.tripId").value(tripId))
                .andExpect(jsonPath("$.title").value("Airport taxi"))
                .andExpect(jsonPath("$.category").value("Ground transport"))
                .andExpect(jsonPath("$.amount").value(65.25))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void updateTripBudgetItemReturnsNotFoundForMissingItem() throws Exception {
        Integer tripId = saveBasicTrip("Missing budget item trip", "Seoul");

        mockMvc.perform(put("/api/trips/{tripId}/budget-items/{itemId}", tripId, 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Missing item",
                                  "category": "General",
                                  "amount": 30.00
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTripBudgetItemReturnsNotFoundWhenItemBelongsToAnotherTrip() throws Exception {
        Integer firstTripId = saveBasicTrip("First budget update trip", "Dubai");
        Integer secondTripId = saveBasicTrip("Second budget update trip", "Lisbon");
        Integer itemId = createTripBudgetItem(firstTripId, "Keep this cost", "General", 15.00);

        mockMvc.perform(put("/api/trips/{tripId}/budget-items/{itemId}", secondTripId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Wrong trip update",
                                  "category": "General",
                                  "amount": 99.00
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/trips/{tripId}/budget-items", firstTripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Keep this cost"));
    }

    @Test
    void deleteTripBudgetItemRemovesItem() throws Exception {
        Integer tripId = saveBasicTrip("Delete budget trip", "Porto");
        Integer itemId = createTripBudgetItem(tripId, "Metro card", "Transport", 20.00);

        mockMvc.perform(delete("/api/trips/{tripId}/budget-items/{itemId}", tripId, itemId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/trips/{tripId}/budget-items", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteTripBudgetItemReturnsNotFoundWhenItemBelongsToAnotherTrip() throws Exception {
        Integer firstTripId = saveBasicTrip("First budget delete trip", "Dubai");
        Integer secondTripId = saveBasicTrip("Second budget delete trip", "Tokyo");
        Integer itemId = createTripBudgetItem(firstTripId, "Keep this budget item", "General", 33.00);

        mockMvc.perform(delete("/api/trips/{tripId}/budget-items/{itemId}", secondTripId, itemId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/trips/{tripId}/budget-items", firstTripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(itemId));
    }

    @Test
    void getTripItineraryReturnsItemsSortedByDayAndStartTime() throws Exception {
        Integer tripId = saveBasicTrip("Itinerary trip", "Dubai");
        insertTripItineraryItem(
                tripId,
                2,
                "Day two breakfast",
                "Start slow",
                "Hotel",
                "08:00:00",
                "09:00:00",
                Instant.parse("2026-01-02T00:00:00Z")
        );
        insertTripItineraryItem(
                tripId,
                1,
                "Museum visit",
                "Explore exhibits",
                "Museum",
                "14:00:00",
                "16:00:00",
                Instant.parse("2026-01-01T00:00:00Z")
        );
        insertTripItineraryItem(
                tripId,
                1,
                "Morning walk",
                "Walk near the hotel",
                "Downtown",
                "09:00:00",
                "10:00:00",
                Instant.parse("2026-01-01T01:00:00Z")
        );

        mockMvc.perform(get("/api/trips/{tripId}/itinerary", tripId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").value("Morning walk"))
                .andExpect(jsonPath("$[0].dayNumber").value(1))
                .andExpect(jsonPath("$[0].startTime").isNotEmpty())
                .andExpect(jsonPath("$[1].title").value("Museum visit"))
                .andExpect(jsonPath("$[2].title").value("Day two breakfast"));
    }

    @Test
    void addTripItineraryItemCreatesItem() throws Exception {
        Integer tripId = saveBasicTrip("Create itinerary trip", "Tokyo");

        mockMvc.perform(post("/api/trips/{tripId}/itinerary", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dayNumber": 2,
                                  "title": "Temple visit",
                                  "description": "Spend the morning at the temple",
                                  "location": "Kyoto",
                                  "startTime": "09:30",
                                  "endTime": "11:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tripId").value(tripId))
                .andExpect(jsonPath("$.dayNumber").value(2))
                .andExpect(jsonPath("$.title").value("Temple visit"))
                .andExpect(jsonPath("$.description").value("Spend the morning at the temple"))
                .andExpect(jsonPath("$.location").value("Kyoto"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void addTripItineraryItemReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(post("/api/trips/{tripId}/itinerary", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dayNumber": 1,
                                  "title": "Missing trip item",
                                  "description": "Missing",
                                  "location": "TBD",
                                  "startTime": "09:00",
                                  "endTime": "10:00"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTripItineraryItemChangesFields() throws Exception {
        Integer tripId = saveBasicTrip("Update itinerary trip", "Rome");
        Integer itemId = createItineraryItem(tripId, 1, "Old title");

        mockMvc.perform(put("/api/trips/{tripId}/itinerary/{itemId}", tripId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dayNumber": 3,
                                  "title": "Updated dinner",
                                  "description": "Dinner near the plaza",
                                  "location": "Main Plaza",
                                  "startTime": "19:00",
                                  "endTime": "20:30"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.tripId").value(tripId))
                .andExpect(jsonPath("$.dayNumber").value(3))
                .andExpect(jsonPath("$.title").value("Updated dinner"))
                .andExpect(jsonPath("$.location").value("Main Plaza"));
    }

    @Test
    void updateTripItineraryItemReturnsNotFoundWhenItemBelongsToAnotherTrip() throws Exception {
        Integer firstTripId = saveBasicTrip("First itinerary trip", "Dubai");
        Integer secondTripId = saveBasicTrip("Second itinerary trip", "Lisbon");
        Integer itemId = createItineraryItem(firstTripId, 1, "Keep this item");

        mockMvc.perform(put("/api/trips/{tripId}/itinerary/{itemId}", secondTripId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dayNumber": 1,
                                  "title": "Wrong trip update",
                                  "description": "Wrong",
                                  "location": "Wrong",
                                  "startTime": "09:00",
                                  "endTime": "10:00"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTripItineraryItemRemovesItem() throws Exception {
        Integer tripId = saveBasicTrip("Delete itinerary trip", "Porto");
        Integer itemId = createItineraryItem(tripId, 1, "Delete this item");

        mockMvc.perform(delete("/api/trips/{tripId}/itinerary/{itemId}", tripId, itemId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/trips/{tripId}/itinerary", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteTripItineraryItemReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(delete("/api/trips/{tripId}/itinerary/{itemId}", 999999L, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTripTagsReturnsTagsSortedByName() throws Exception {
        Integer tripId = saveBasicTrip("Tag trip", "Dubai");
        insertTripTag(tripId, "Luxury", Instant.parse("2026-01-01T00:00:00Z"));
        insertTripTag(tripId, "Beach", Instant.parse("2026-01-02T00:00:00Z"));

        mockMvc.perform(get("/api/trips/{tripId}/tags", tripId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Beach"))
                .andExpect(jsonPath("$[1].name").value("Luxury"));
    }

    @Test
    void addTripTagCreatesTagAndPreventsDuplicates() throws Exception {
        Integer tripId = saveBasicTrip("Create tag trip", "Tokyo");

        MvcResult firstResult = mockMvc.perform(post("/api/trips/{tripId}/tags", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Culture"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripId").value(tripId))
                .andExpect(jsonPath("$.name").value("Culture"))
                .andReturn();

        Integer firstTagId = JsonPath.read(firstResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/trips/{tripId}/tags", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "culture"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstTagId));

        mockMvc.perform(get("/api/trips/{tripId}/tags", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void addTripTagReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(post("/api/trips/{tripId}/tags", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Missing"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTripTagRemovesTag() throws Exception {
        Integer tripId = saveBasicTrip("Delete tag trip", "Seoul");
        Integer tagId = createTripTag(tripId, "Family");

        mockMvc.perform(delete("/api/trips/{tripId}/tags/{tagId}", tripId, tagId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/trips/{tripId}/tags", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteTripTagReturnsNotFoundWhenTagBelongsToAnotherTrip() throws Exception {
        Integer firstTripId = saveBasicTrip("First tag trip", "Dubai");
        Integer secondTripId = saveBasicTrip("Second tag trip", "Tokyo");
        Integer tagId = createTripTag(firstTripId, "Keep tag");

        mockMvc.perform(delete("/api/trips/{tripId}/tags/{tagId}", secondTripId, tagId))
                .andExpect(status().isNotFound());
    }

    @Test
    void exportTripReturnsPlainTextWithAllSections() throws Exception {
        Integer tripId = saveBasicTrip("Export trip", "Dubai");
        createTripNote(tripId, "Remember airport transfer");
        createChecklistItem(tripId, "Pack passport");
        createTripDocument(tripId, "Passport", "Passport", "https://example.com/passport");
        createTripBudgetItem(tripId, "Flight", "Transport", 850.00);
        createItineraryItem(tripId, 1, "Arrival");
        createTripTag(tripId, "Luxury");

        mockMvc.perform(get("/api/trips/{tripId}/export", tripId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("AI Travel Agent Trip Export")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Destination: Dubai")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Remember airport transfer")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pack passport")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Passport")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Flight")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Arrival")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Luxury")));
    }

    @Test
    void exportTripReturnsNotFoundForMissingTrip() throws Exception {
        mockMvc.perform(get("/api/trips/{tripId}/export", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void dashboardSummaryReturnsAggregateCountsAndRecentTrips() throws Exception {
        Integer firstTripId = saveBasicTrip("First dashboard trip", "Dubai");
        Integer secondTripId = saveBasicTrip("Second dashboard trip", "Tokyo");
        mockMvc.perform(patch("/api/trips/{id}/favorite", firstTripId))
                .andExpect(status().isOk());
        createTripNote(firstTripId, "Dashboard note");
        createChecklistItem(firstTripId, "Incomplete checklist");
        Integer completedChecklistId = createChecklistItem(firstTripId, "Completed checklist");
        mockMvc.perform(patch("/api/trips/{tripId}/checklist/{itemId}", firstTripId, completedChecklistId))
                .andExpect(status().isOk());
        createTripDocument(firstTripId, "Visa", "Visa", "https://example.com/visa");
        createTripBudgetItem(firstTripId, "Flight", "Transport", 850.00);
        createTripBudgetItem(secondTripId, "Hotel", "Lodging", 150.50);
        createItineraryItem(firstTripId, 1, "Dashboard itinerary");
        createTripTag(firstTripId, "Dashboard");

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTrips").value(2))
                .andExpect(jsonPath("$.favoriteTrips").value(1))
                .andExpect(jsonPath("$.totalNotes").value(1))
                .andExpect(jsonPath("$.totalChecklistItems").value(2))
                .andExpect(jsonPath("$.completedChecklistItems").value(1))
                .andExpect(jsonPath("$.totalDocuments").value(1))
                .andExpect(jsonPath("$.totalBudgetAmount").value(1000.50))
                .andExpect(jsonPath("$.totalItineraryItems").value(1))
                .andExpect(jsonPath("$.totalTags").value(1))
                .andExpect(jsonPath("$.recentTrips.length()").value(2));
    }

    @Test
    void deleteTripRemovesSavedTripWithAllChildResources() throws Exception {
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
        mockMvc.perform(post("/api/trips/{tripId}/checklist", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Pack luggage"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/trips/{tripId}/documents", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Passport",
                                  "type": "Passport",
                                  "url": "https://example.com/passport"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/trips/{tripId}/budget-items", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Flight",
                                  "category": "Transport",
                                  "amount": 850.00
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/trips/{tripId}/itinerary", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dayNumber": 1,
                                  "title": "Arrival",
                                  "description": "Arrive and check in",
                                  "location": "Hotel",
                                  "startTime": "15:00",
                                  "endTime": "16:00"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/trips/{tripId}/tags", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Family"
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

    private Integer saveBasicTrip(String userMessage, String destination) throws Exception {
        MvcResult saveResult = mockMvc.perform(post("/api/trips/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMessage": "%s",
                                  "origin": "Austin",
                                  "destination": "%s",
                                  "budget": 1500,
                                  "days": 7
                                }
                                """.formatted(userMessage, destination)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(saveResult.getResponse().getContentAsString(), "$.id");
    }

    private Integer createTripNote(Integer tripId, String content) throws Exception {
        MvcResult noteResult = mockMvc.perform(post("/api/trips/{id}/notes", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "%s"
                                }
                                """.formatted(content)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(noteResult.getResponse().getContentAsString(), "$.id");
    }

    private Integer createChecklistItem(Integer tripId, String title) throws Exception {
        MvcResult itemResult = mockMvc.perform(post("/api/trips/{tripId}/checklist", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s"
                                }
                                """.formatted(title)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(itemResult.getResponse().getContentAsString(), "$.id");
    }

    private Integer createTripDocument(Integer tripId, String name, String type, String url) throws Exception {
        MvcResult documentResult = mockMvc.perform(post("/api/trips/{tripId}/documents", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "type": "%s",
                                  "url": "%s"
                                }
                                """.formatted(name, type, url)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(documentResult.getResponse().getContentAsString(), "$.id");
    }

    private Integer createTripBudgetItem(
            Integer tripId,
            String title,
            String category,
            double amount
    ) throws Exception {
        MvcResult itemResult = mockMvc.perform(post("/api/trips/{tripId}/budget-items", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "category": "%s",
                                  "amount": %s
                                }
                                """.formatted(title, category, Double.toString(amount))))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(itemResult.getResponse().getContentAsString(), "$.id");
    }

    private Integer createItineraryItem(Integer tripId, int dayNumber, String title) throws Exception {
        MvcResult itemResult = mockMvc.perform(post("/api/trips/{tripId}/itinerary", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dayNumber": %s,
                                  "title": "%s",
                                  "description": "Sample itinerary item",
                                  "location": "Sample location",
                                  "startTime": "09:00",
                                  "endTime": "10:00"
                                }
                                """.formatted(Integer.toString(dayNumber), title)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(itemResult.getResponse().getContentAsString(), "$.id");
    }

    private Integer createTripTag(Integer tripId, String name) throws Exception {
        MvcResult tagResult = mockMvc.perform(post("/api/trips/{tripId}/tags", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s"
                                }
                                """.formatted(name)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(tagResult.getResponse().getContentAsString(), "$.id");
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

    private void insertChecklistItem(Integer tripId, String title, boolean completed, Instant createdAt) {
        jdbcTemplate.update("""
                INSERT INTO trip_checklist_items (
                  trip_id,
                  title,
                  completed,
                  created_at
                )
                VALUES (?, ?, ?, ?)
                """,
                tripId,
                title,
                completed,
                Timestamp.from(createdAt)
        );
    }

    private void insertTripDocument(
            Integer tripId,
            String name,
            String type,
            String url,
            Instant createdAt
    ) {
        jdbcTemplate.update("""
                INSERT INTO trip_documents (
                  trip_id,
                  name,
                  type,
                  url,
                  created_at
                )
                VALUES (?, ?, ?, ?, ?)
                """,
                tripId,
                name,
                type,
                url,
                Timestamp.from(createdAt)
        );
    }

    private void insertTripBudgetItem(
            Integer tripId,
            String title,
            String category,
            double amount,
            Instant createdAt
    ) {
        jdbcTemplate.update("""
                INSERT INTO trip_budget_items (
                  trip_id,
                  title,
                  category,
                  amount,
                  created_at,
                  updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                tripId,
                title,
                category,
                amount,
                Timestamp.from(createdAt),
                Timestamp.from(createdAt)
        );
    }

    private void insertTripItineraryItem(
            Integer tripId,
            int dayNumber,
            String title,
            String description,
            String location,
            String startTime,
            String endTime,
            Instant createdAt
    ) {
        jdbcTemplate.update("""
                INSERT INTO trip_itinerary_items (
                  trip_id,
                  day_number,
                  title,
                  description,
                  location,
                  start_time,
                  end_time,
                  created_at,
                  updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                tripId,
                dayNumber,
                title,
                description,
                location,
                startTime,
                endTime,
                Timestamp.from(createdAt),
                Timestamp.from(createdAt)
        );
    }

    private void insertTripTag(Integer tripId, String name, Instant createdAt) {
        jdbcTemplate.update("""
                INSERT INTO trip_tags (
                  trip_id,
                  name,
                  created_at
                )
                VALUES (?, ?, ?)
                """,
                tripId,
                name,
                Timestamp.from(createdAt)
        );
    }
}
