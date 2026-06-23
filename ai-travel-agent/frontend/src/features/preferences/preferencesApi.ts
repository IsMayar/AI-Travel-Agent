import { api } from "../../services/api";

export interface TravelPreferences {
  preferredBudget: number;
  preferredDuration: number;
  preferredTravelStyle: string;
  preferredDestination: string;
}

export const preferencesApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getPreferences: builder.query<TravelPreferences, void>({
      query: () => "/api/preferences",
      providesTags: ["Preferences"],
    }),
    updatePreferences: builder.mutation<TravelPreferences, TravelPreferences>({
      query: (body) => ({
        url: "/api/preferences",
        method: "PUT",
        body,
      }),
      invalidatesTags: ["Preferences"],
    }),
  }),
});

export const {
  useGetPreferencesQuery,
  useUpdatePreferencesMutation,
} = preferencesApi;
