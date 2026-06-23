import type { SavedTrip } from "../features/trips/tripsApi";

interface TripCardProps {
  trip: SavedTrip;
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

export function TripCard({ trip }: TripCardProps) {
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
    </article>
  );
}
