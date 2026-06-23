import type { FlightOption } from "../features/trips/tripsApi";

interface FlightCardProps {
  flight: FlightOption;
}

export function FlightCard({ flight }: FlightCardProps) {
  return (
    <article className="option-card">
      <div>
        <p className="eyebrow">Flight</p>
        <h3>{flight.airline}</h3>
      </div>
      <div className="option-meta">
        <span>${flight.price.toLocaleString()}</span>
        <span>{flight.duration}</span>
      </div>
    </article>
  );
}
