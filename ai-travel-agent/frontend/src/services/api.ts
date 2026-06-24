import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

const baseQuery = fetchBaseQuery({
  baseUrl: "http://localhost:8080",
  prepareHeaders: (headers, { getState }) => {
    const state = getState() as {
      auth?: {
        token?: string | null;
      };
    };

    if (state.auth?.token) {
      headers.set("authorization", `Bearer ${state.auth.token}`);
    }

    return headers;
  },
});

export const api = createApi({
  reducerPath: "api",
  baseQuery,
  tagTypes: [
    "Auth",
    "Dashboard",
    "Preferences",
    "SavedTrips",
    "TripBudgetItems",
    "TripChecklist",
    "TripDocuments",
    "TripItinerary",
    "TripNotes",
    "TripTags",
  ],
  endpoints: () => ({}),
});
