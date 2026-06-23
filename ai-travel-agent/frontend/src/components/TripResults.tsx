import type { TripPlanResponse } from "../features/trips/tripsApi";
import {
  recommendedActivities,
  travelTips,
} from "../data/travelData";
import { BudgetCard } from "./BudgetCard";
import { FlightCard } from "./FlightCard";
import { HotelCard } from "./HotelCard";
import { ItineraryCard } from "./ItineraryCard";
import { LoadingSpinner } from "./LoadingSpinner";
import { SectionTitle } from "./SectionTitle";

interface TripResultsProps {
  tripPlan: TripPlanResponse;
  isSaving: boolean;
  saveMessage: string | null;
  onSave: () => void;
}

export function TripResults({
  tripPlan,
  isSaving,
  saveMessage,
  onSave,
}: TripResultsProps) {
  return (
    <section className="trip-results" aria-live="polite">
      <div className="results-header">
        <SectionTitle
          eyebrow="Trip results"
          title={`${tripPlan.destination} plan`}
          description="A clean mock plan with the core pieces you need for the next planning pass."
        />
        <button className="secondary-button" type="button" onClick={onSave} disabled={isSaving}>
          {isSaving ? (
            <>
              <LoadingSpinner label="Saving trip" />
              Saving
            </>
          ) : (
            "Save Trip"
          )}
        </button>
      </div>

      {saveMessage && <p className="inline-note">{saveMessage}</p>}

      <div className="results-layout">
        <BudgetCard
          origin={tripPlan.origin}
          destination={tripPlan.destination}
          budget={tripPlan.budget}
          days={tripPlan.days}
        />

        <div className="result-panel">
          <h3>Flight options</h3>
          <div className="stacked-list">
            {tripPlan.flightOptions.map((flight) => (
              <FlightCard flight={flight} key={`${flight.airline}-${flight.price}`} />
            ))}
          </div>
        </div>

        <div className="result-panel">
          <h3>Hotel options</h3>
          <div className="stacked-list">
            {tripPlan.hotelOptions.map((hotel) => (
              <HotelCard hotel={hotel} key={hotel.name} />
            ))}
          </div>
        </div>
      </div>

      <div className="two-column-section">
        <div>
          <h3>Day-by-day itinerary</h3>
          <div className="itinerary-stack">
            {tripPlan.itinerary.map((day) => (
              <ItineraryCard day={day} key={day.day} />
            ))}
          </div>
        </div>
        <aside className="tips-panel">
          <h3>Travel tips</h3>
          <ul>
            {travelTips.map((tip) => (
              <li key={tip}>{tip}</li>
            ))}
          </ul>
          <h3>Recommended activities</h3>
          <div className="activity-tags">
            {recommendedActivities.map((activity) => (
              <span key={activity}>{activity}</span>
            ))}
          </div>
        </aside>
      </div>
    </section>
  );
}
