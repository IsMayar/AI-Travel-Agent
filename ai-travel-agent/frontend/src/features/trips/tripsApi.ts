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
  useDeleteSavedTripMutation,
  useDuplicateSavedTripMutation,
  useGetSavedTripQuery,
  useGetSavedTripsQuery,
  usePlanTripMutation,
  useSearchSavedTripsQuery,
  useSaveTripMutation,
  useToggleFavoriteTripMutation,
  useUpdateSavedTripMutation,
} = tripsApi;
