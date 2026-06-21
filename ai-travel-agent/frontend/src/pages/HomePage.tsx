import { FormEvent, useState } from "react";

import { planTrip, TripPlanResponse } from "../services/trips";

const placeholder = "Plan a 7-day trip from Austin to Dubai under $1500";

export function HomePage() {
  const [message, setMessage] = useState("");
  const [tripPlan, setTripPlan] = useState<TripPlanResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const trimmedMessage = message.trim();
    if (!trimmedMessage) {
      setError("Enter a travel request to plan a trip.");
      setTripPlan(null);
      return;
    }

    setIsLoading(true);
    setError(null);
    setTripPlan(null);

    try {
      const result = await planTrip(trimmedMessage);
      setTripPlan(result);
    } catch {
      setError("Could not plan the trip right now. Please try again.");
    } finally {
      setIsLoading(false);
    }
  }

  const tripSummary = tripPlan
    ? [
        { label: "Origin", value: tripPlan.origin },
        { label: "Destination", value: tripPlan.destination },
        { label: "Budget", value: `$${tripPlan.budget.toLocaleString()}` },
        { label: "Days", value: tripPlan.days.toString() }
      ]
    : [];

  return (
    <main className="app-shell">
      <section className="planner">
        <div className="planner-header">
          <p className="eyebrow">Trip planning assistant</p>
          <h1>AI Travel Agent</h1>
        </div>

        <form className="planner-form" onSubmit={handleSubmit}>
          <label htmlFor="travel-request">Travel request</label>
          <textarea
            id="travel-request"
            value={message}
            onChange={(event) => setMessage(event.target.value)}
            placeholder={placeholder}
            rows={6}
          />
          <button type="submit" disabled={isLoading}>
            {isLoading ? "Planning..." : "Plan Trip"}
          </button>
        </form>

        {isLoading && (
          <p className="status-message" role="status">
            Building a draft itinerary...
          </p>
        )}

        {error && (
          <p className="status-message error" role="alert">
            {error}
          </p>
        )}

        {tripPlan && (
          <section className="result" aria-live="polite">
            <div>
              <p className="eyebrow">Mock trip plan</p>
              <h2>
                {tripPlan.days} days from {tripPlan.origin} to{" "}
                {tripPlan.destination}
              </h2>
              <p className="summary">Budget: ${tripPlan.budget}</p>
            </div>

            <div className="summary-grid">
              {tripSummary.map((item) => (
                <article className="info-card compact" key={item.label}>
                  <span>{item.label}</span>
                  <strong>{item.value}</strong>
                </article>
              ))}
            </div>

            <div className="result-grid">
              <article className="info-card">
                <h3>Flight options</h3>
                {tripPlan.flightOptions.map((flight) => (
                  <div className="option-row" key={`${flight.airline}-${flight.price}`}>
                    <strong>{flight.airline}</strong>
                    <span>${flight.price}</span>
                    <span>{flight.duration}</span>
                  </div>
                ))}
              </article>

              <article className="info-card">
                <h3>Hotel options</h3>
                {tripPlan.hotelOptions.map((hotel) => (
                  <div className="option-row" key={hotel.name}>
                    <strong>{hotel.name}</strong>
                    <span>${hotel.pricePerNight}/night</span>
                    <span>{hotel.rating}/5</span>
                  </div>
                ))}
              </article>
            </div>

            <section className="itinerary-list">
              <h3>Itinerary</h3>
              {tripPlan.itinerary.map((item) => (
                <article className="info-card itinerary-item" key={item.day}>
                  <strong>
                    Day {item.day}: {item.title}
                  </strong>
                  <ul>
                    {item.activities.map((activity) => (
                      <li key={activity}>{activity}</li>
                    ))}
                  </ul>
                </article>
              ))}
            </section>
          </section>
        )}
      </section>
    </main>
  );
}
