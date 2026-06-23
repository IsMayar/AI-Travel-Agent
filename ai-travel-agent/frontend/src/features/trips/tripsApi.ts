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
  createdAt: string;
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
    getSavedTrips: builder.query<SavedTrip[], void>({
      query: () => "/api/trips",
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
  useGetSavedTripsQuery,
  usePlanTripMutation,
  useSaveTripMutation,
} = tripsApi;
