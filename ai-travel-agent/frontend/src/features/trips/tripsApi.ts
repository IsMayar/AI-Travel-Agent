import { api } from "../../services/api";

export interface FlightOption {
  airline: string;
  price: number;
  duration: string;
}

export interface HotelOption {
  name: string;
  pricePerNight: number;
  rating: number;
}

export interface ItineraryDay {
  day: number;
  title: string;
  activities: string[];
}

export interface TripPlanRequest {
  message: string;
}

export interface TripPlanResponse {
  destination: string;
  origin: string;
  budget: number;
  days: number;
  flightOptions: FlightOption[];
  hotelOptions: HotelOption[];
  itinerary: ItineraryDay[];
}

export interface SaveTripRequest {
  userMessage: string;
  origin: string;
  destination: string;
  budget: number;
  days: number;
}

export interface SavedTrip {
  id: number;
  userMessage: string;
  origin: string;
  destination: string;
  budget: number;
  days: number;
  favorite: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TripNoteRequest {
  content: string;
}

export interface TripNoteUpdateRequest {
  content: string;
}

export interface TripNote {
  id: number;
  tripId: number;
  content: string;
  createdAt: string;
}

export interface TripChecklistItemRequest {
  title: string;
}

export interface TripChecklistItem {
  id: number;
  tripId: number;
  title: string;
  completed: boolean;
  createdAt: string;
}

export interface TripStatsResponse {
  totalTrips: number;
  favoriteTrips: number;
  averageBudget: number;
  mostCommonDestination: string;
}

export interface TripRecommendation {
  origin: string;
  destination: string;
  budget: number;
  days: number;
  travelStyle: string;
  reason: string;
}

export interface TripRecommendationsResponse {
  recommendations: TripRecommendation[];
}

export const tripsApi = api.injectEndpoints({
  endpoints: (builder) => ({
    planTrip: builder.mutation<TripPlanResponse, TripPlanRequest>({
      query: (body) => ({
        url: "/api/trips/plan",
        method: "POST",
        body,
      }),
    }),
    saveTrip: builder.mutation<SavedTrip, SaveTripRequest>({
      query: (body) => ({
        url: "/api/trips/save",
        method: "POST",
        body,
      }),
      invalidatesTags: [{ type: "SavedTrips", id: "LIST" }],
    }),
    deleteSavedTrip: builder.mutation<void, number>({
      query: (id) => ({
        url: `/api/trips/${id}`,
        method: "DELETE",
      }),
      invalidatesTags: (_result, _error, id) => [
        { type: "SavedTrips", id },
        { type: "SavedTrips", id: "LIST" },
      ],
    }),
    updateSavedTrip: builder.mutation<
      SavedTrip,
      { id: number; trip: SaveTripRequest }
    >({
      query: ({ id, trip }) => ({
        url: `/api/trips/${id}`,
        method: "PUT",
        body: trip,
      }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: "SavedTrips", id },
        { type: "SavedTrips", id: "LIST" },
      ],
    }),
    toggleFavoriteTrip: builder.mutation<SavedTrip, number>({
      query: (id) => ({
        url: `/api/trips/${id}/favorite`,
        method: "PATCH",
      }),
      invalidatesTags: (_result, _error, id) => [
        { type: "SavedTrips", id },
        { type: "SavedTrips", id: "LIST" },
      ],
    }),
    duplicateSavedTrip: builder.mutation<SavedTrip, number>({
      query: (id) => ({
        url: `/api/trips/${id}/duplicate`,
        method: "POST",
      }),
      invalidatesTags: [{ type: "SavedTrips", id: "LIST" }],
    }),
    addTripNote: builder.mutation<
      TripNote,
      { tripId: number; note: TripNoteRequest }
    >({
      query: ({ tripId, note }) => ({
        url: `/api/trips/${tripId}/notes`,
        method: "POST",
        body: note,
      }),
      invalidatesTags: (_result, _error, { tripId }) => [
        { type: "TripNotes", id: tripId },
      ],
    }),
    getTripNotes: builder.query<TripNote[], number>({
      query: (tripId) => `/api/trips/${tripId}/notes`,
      providesTags: (_result, _error, tripId) => [
        { type: "TripNotes", id: tripId },
      ],
    }),
    updateTripNote: builder.mutation<
      TripNote,
      { tripId: number; noteId: number; note: TripNoteUpdateRequest }
    >({
      query: ({ tripId, noteId, note }) => ({
        url: `/api/trips/${tripId}/notes/${noteId}`,
        method: "PUT",
        body: note,
      }),
      invalidatesTags: (_result, _error, { tripId }) => [
        { type: "TripNotes", id: tripId },
      ],
    }),
    deleteTripNote: builder.mutation<
      void,
      { tripId: number; noteId: number }
    >({
      query: ({ tripId, noteId }) => ({
        url: `/api/trips/${tripId}/notes/${noteId}`,
        method: "DELETE",
      }),
      invalidatesTags: (_result, _error, { tripId }) => [
        { type: "TripNotes", id: tripId },
      ],
    }),
    getTripChecklist: builder.query<TripChecklistItem[], number>({
      query: (tripId) => `/api/trips/${tripId}/checklist`,
      providesTags: (_result, _error, tripId) => [
        { type: "TripChecklist", id: tripId },
      ],
    }),
    addTripChecklistItem: builder.mutation<
      TripChecklistItem,
      { tripId: number; item: TripChecklistItemRequest }
    >({
      query: ({ tripId, item }) => ({
        url: `/api/trips/${tripId}/checklist`,
        method: "POST",
        body: item,
      }),
      invalidatesTags: (_result, _error, { tripId }) => [
        { type: "TripChecklist", id: tripId },
      ],
    }),
    toggleTripChecklistItem: builder.mutation<
      TripChecklistItem,
      { tripId: number; itemId: number }
    >({
      query: ({ tripId, itemId }) => ({
        url: `/api/trips/${tripId}/checklist/${itemId}`,
        method: "PATCH",
      }),
      invalidatesTags: (_result, _error, { tripId }) => [
        { type: "TripChecklist", id: tripId },
      ],
    }),
    deleteTripChecklistItem: builder.mutation<
      void,
      { tripId: number; itemId: number }
    >({
      query: ({ tripId, itemId }) => ({
        url: `/api/trips/${tripId}/checklist/${itemId}`,
        method: "DELETE",
      }),
      invalidatesTags: (_result, _error, { tripId }) => [
        { type: "TripChecklist", id: tripId },
      ],
    }),
    getSavedTrips: builder.query<SavedTrip[], { favorite?: boolean } | void>({
      query: (args) => ({
        url: "/api/trips",
        params:
          args && args.favorite !== undefined
            ? { favorite: String(args.favorite) }
            : undefined,
      }),
      providesTags: (result) =>
        result
          ? [
              ...result.map((trip) => ({
                type: "SavedTrips" as const,
                id: trip.id,
              })),
              { type: "SavedTrips", id: "LIST" },
            ]
          : [{ type: "SavedTrips", id: "LIST" }],
    }),
    getRecentTrips: builder.query<SavedTrip[], void>({
      query: () => "/api/trips/recent",
      providesTags: (result) =>
        result
          ? [
              ...result.map((trip) => ({
                type: "SavedTrips" as const,
                id: trip.id,
              })),
              { type: "SavedTrips", id: "LIST" },
            ]
          : [{ type: "SavedTrips", id: "LIST" }],
    }),
    getTripStats: builder.query<TripStatsResponse, void>({
      query: () => "/api/trips/stats",
      providesTags: [{ type: "SavedTrips", id: "LIST" }],
    }),
    getTripRecommendations: builder.query<TripRecommendationsResponse, void>({
      query: () => "/api/trips/recommendations",
      providesTags: [
        { type: "SavedTrips", id: "LIST" },
        "Preferences",
      ],
    }),
    getSavedTrip: builder.query<SavedTrip, number>({
      query: (id) => `/api/trips/${id}`,
      providesTags: (_result, _error, id) => [{ type: "SavedTrips", id }],
    }),
    searchSavedTrips: builder.query<SavedTrip[], string>({
      query: (query) => ({
        url: "/api/trips/search",
        params: { q: query },
      }),
      providesTags: (result) =>
        result
          ? [
              ...result.map((trip) => ({
                type: "SavedTrips" as const,
                id: trip.id,
              })),
              { type: "SavedTrips", id: "LIST" },
            ]
          : [{ type: "SavedTrips", id: "LIST" }],
    }),
  }),
});

export const {
  useAddTripNoteMutation,
  useAddTripChecklistItemMutation,
  useDeleteTripChecklistItemMutation,
  useDeleteTripNoteMutation,
  useDeleteSavedTripMutation,
  useDuplicateSavedTripMutation,
  useGetTripChecklistQuery,
  useGetRecentTripsQuery,
  useGetSavedTripQuery,
  useGetSavedTripsQuery,
  useGetTripRecommendationsQuery,
  useGetTripNotesQuery,
  useGetTripStatsQuery,
  usePlanTripMutation,
  useSearchSavedTripsQuery,
  useSaveTripMutation,
  useToggleTripChecklistItemMutation,
  useToggleFavoriteTripMutation,
  useUpdateTripNoteMutation,
  useUpdateSavedTripMutation,
} = tripsApi;
