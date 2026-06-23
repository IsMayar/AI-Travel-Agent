import type { SavedTrip } from "../features/trips/tripsApi";

interface TripCardProps {
  trip: SavedTrip;
  isDeleting?: boolean;
  onDelete?: (trip: SavedTrip) => void;
}

function formatDate(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "Saved trip";
  }

  return new Intl.DateTimeFormat("en", {
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(date);
}

export function TripCard({ trip, isDeleting = false, onDelete }: TripCardProps) {
  return (
    <article className="trip-card">
      <div>
        <p className="eyebrow">{formatDate(trip.createdAt)}</p>
        <h3>
          {trip.origin} to {trip.destination}
        </h3>
        <p>{trip.userMessage}</p>
      </div>
      <div className="trip-card-meta">
        <span>{trip.days} days</span>
        <span>${trip.budget.toLocaleString()}</span>
      </div>
      {onDelete && (
        <div className="trip-card-actions">
          <button
            className="danger-button"
            disabled={isDeleting}
            onClick={() => onDelete(trip)}
            type="button"
          >
            {isDeleting ? "Deleting..." : "Delete"}
          </button>
        </div>
      )}
    </article>
  );
}
