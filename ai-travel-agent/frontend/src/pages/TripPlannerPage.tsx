import { FormEvent, useState } from "react";

import { Container } from "../components/Container";
import { ErrorState } from "../components/ErrorState";
import { LoadingSpinner } from "../components/LoadingSpinner";
import { PromptChip } from "../components/PromptChip";
import { SectionTitle } from "../components/SectionTitle";
import { TripResults } from "../components/TripResults";
import {
  SaveTripRequest,
  TripPlanResponse,
  usePlanTripMutation,
  useSaveTripMutation,
} from "../features/trips/tripsApi";
import { demoTripPlan, promptSamples } from "../data/travelData";

function getInitialPrompt() {
  return new URLSearchParams(window.location.search).get("prompt") ?? "";
}

function toSaveTripRequest(
  userMessage: string,
  tripPlan: TripPlanResponse
): SaveTripRequest {
  return {
    userMessage,
    origin: tripPlan.origin,
    destination: tripPlan.destination,
    budget: tripPlan.budget,
    days: tripPlan.days,
  };
}

function LoadingSkeletons() {
  return (
    <section className="skeleton-section" aria-label="Loading trip plan">
      <div className="skeleton-card large" />
      <div className="skeleton-grid">
        <div className="skeleton-card" />
        <div className="skeleton-card" />
        <div className="skeleton-card" />
      </div>
    </section>
  );
}

export function TripPlannerPage() {
  const [message, setMessage] = useState(getInitialPrompt);
  const [submittedMessage, setSubmittedMessage] = useState("");
  const [tripPlan, setTripPlan] = useState<TripPlanResponse | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [saveMessage, setSaveMessage] = useState<string | null>(null);
  const [planTrip, { isLoading }] = usePlanTripMutation();
  const [saveTrip, { isLoading: isSaving }] = useSaveTripMutation();

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const trimmedMessage = message.trim();
    if (!trimmedMessage) {
      setErrorMessage("Enter a travel request to plan a trip.");
      setTripPlan(null);
      return;
    }

    setErrorMessage(null);
    setSaveMessage(null);
    setTripPlan(null);
    setSubmittedMessage(trimmedMessage);

    try {
      const result = await planTrip({ message: trimmedMessage }).unwrap();
      setTripPlan(result);
    } catch {
      setErrorMessage(
        "Backend is unavailable, so a fallback demo trip is shown below."
      );
      setTripPlan(demoTripPlan);
    }
  }

  async function handleSaveTrip() {
    if (!tripPlan) {
      return;
    }

    setSaveMessage(null);

    try {
      await saveTrip(toSaveTripRequest(submittedMessage || message, tripPlan)).unwrap();
      setSaveMessage("Trip saved. It will appear on the Saved Trips page.");
    } catch {
      setSaveMessage("Could not save this trip because the backend is unavailable.");
    }
  }

  return (
    <main>
      <section className="planner-hero">
        <Container className="planner-grid">
          <div>
            <p className="eyebrow">Trip planner</p>
            <h1>Describe the trip you want</h1>
            <p>
              Add the route, budget, dates, travel style, or constraints. The
              MVP uses mock backend data and will fall back to a demo plan when
              the backend is offline.
            </p>
          </div>
          <form className="planner-card" onSubmit={handleSubmit}>
            <label htmlFor="travel-request">Travel request</label>
            <textarea
              id="travel-request"
              onChange={(event) => setMessage(event.target.value)}
              placeholder="Plan a 7-day trip from Austin to Dubai under $1500"
              rows={7}
              value={message}
            />
            <button className="primary-button" disabled={isLoading} type="submit">
              {isLoading ? (
                <>
                  <LoadingSpinner label="Planning trip" />
                  Planning
                </>
              ) : (
                "Plan Trip"
              )}
            </button>
            <div className="prompt-row">
              {promptSamples.map((prompt) => (
                <PromptChip key={prompt} onClick={() => setMessage(prompt)}>
                  {prompt}
                </PromptChip>
              ))}
            </div>
          </form>
        </Container>
      </section>

      <section className="page-section">
        <Container>
          <SectionTitle
            eyebrow="Trip results"
            title="Your generated plan"
            description="Review the trip summary, compare mock options, and save the plan when it looks useful."
          />

          {errorMessage && (
            <ErrorState title="Planner notice" message={errorMessage} />
          )}

          {isLoading && <LoadingSkeletons />}

          {!isLoading && !tripPlan && !errorMessage && (
            <div className="pre-results-panel">
              <h3>Ready when you are</h3>
              <p>
                Submit a travel request to see budget, flights, hotels, travel
                tips, recommended activities, and a day-by-day itinerary.
              </p>
            </div>
          )}

          {!isLoading && tripPlan && (
            <TripResults
              isSaving={isSaving}
              onSave={handleSaveTrip}
              saveMessage={saveMessage}
              tripPlan={tripPlan}
            />
          )}
        </Container>
      </section>
    </main>
  );
}
