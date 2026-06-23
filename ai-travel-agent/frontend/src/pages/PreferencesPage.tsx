import { FormEvent, useEffect, useState } from "react";

import { Container } from "../components/Container";
import { ErrorState } from "../components/ErrorState";
import { SectionTitle } from "../components/SectionTitle";
import {
  TravelPreferences,
  useGetPreferencesQuery,
  useUpdatePreferencesMutation,
} from "../features/preferences/preferencesApi";

const defaultPreferences: TravelPreferences = {
  preferredBudget: 1500,
  preferredDuration: 7,
  preferredTravelStyle: "Relaxed",
  preferredDestination: "Dubai",
};

export function PreferencesPage() {
  const preferencesQuery = useGetPreferencesQuery();
  const [updatePreferences, updatePreferencesState] =
    useUpdatePreferencesMutation();
  const [values, setValues] = useState<TravelPreferences>(defaultPreferences);

  useEffect(() => {
    if (preferencesQuery.data) {
      setValues(preferencesQuery.data);
    }
  }, [preferencesQuery.data]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const updatedPreferences = await updatePreferences(values).unwrap();
    setValues(updatedPreferences);
  }

  return (
    <main className="simple-page">
      <Container>
        <SectionTitle
          eyebrow="Preferences"
          title="Travel preferences"
          description="Set default trip preferences for budget, duration, travel style, and destination."
        />

        {preferencesQuery.isLoading && (
          <div className="preferences-card">
            <div className="skeleton-card large" />
          </div>
        )}

        {!preferencesQuery.isLoading && preferencesQuery.isError && (
          <ErrorState
            actionLabel="Try Again"
            message="Travel preferences could not be loaded from the backend."
            onAction={() => preferencesQuery.refetch()}
            title="Could not load preferences"
          />
        )}

        {!preferencesQuery.isLoading && !preferencesQuery.isError && (
          <section className="preferences-card">
            <form className="preferences-form" onSubmit={handleSubmit}>
              <div className="form-grid">
                <label>
                  Preferred budget
                  <input
                    min="1"
                    onChange={(event) =>
                      setValues((current) => ({
                        ...current,
                        preferredBudget: Number(event.target.value),
                      }))
                    }
                    type="number"
                    value={values.preferredBudget}
                  />
                </label>

                <label>
                  Preferred duration
                  <input
                    min="1"
                    onChange={(event) =>
                      setValues((current) => ({
                        ...current,
                        preferredDuration: Number(event.target.value),
                      }))
                    }
                    type="number"
                    value={values.preferredDuration}
                  />
                </label>

                <label>
                  Travel style
                  <select
                    onChange={(event) =>
                      setValues((current) => ({
                        ...current,
                        preferredTravelStyle: event.target.value,
                      }))
                    }
                    value={values.preferredTravelStyle}
                  >
                    <option value="Relaxed">Relaxed</option>
                    <option value="Adventure">Adventure</option>
                    <option value="Culture">Culture</option>
                    <option value="Family">Family</option>
                    <option value="Luxury">Luxury</option>
                    <option value="Budget">Budget</option>
                  </select>
                </label>

                <label>
                  Preferred destination
                  <input
                    onChange={(event) =>
                      setValues((current) => ({
                        ...current,
                        preferredDestination: event.target.value,
                      }))
                    }
                    type="text"
                    value={values.preferredDestination}
                  />
                </label>
              </div>

              {updatePreferencesState.isError && (
                <p className="preferences-message error-text">
                  Preferences could not be saved. Please try again.
                </p>
              )}

              {updatePreferencesState.isSuccess && (
                <p className="preferences-message success-text">
                  Preferences saved.
                </p>
              )}

              <div className="form-actions">
                <button
                  className="primary-button"
                  disabled={updatePreferencesState.isLoading}
                  type="submit"
                >
                  {updatePreferencesState.isLoading
                    ? "Saving..."
                    : "Save Preferences"}
                </button>
              </div>
            </form>
          </section>
        )}
      </Container>
    </main>
  );
}
