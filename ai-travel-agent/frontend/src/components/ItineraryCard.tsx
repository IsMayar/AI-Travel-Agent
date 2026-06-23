import type { ItineraryDay } from "../features/trips/tripsApi";

interface ItineraryCardProps {
  day: ItineraryDay;
}

export function ItineraryCard({ day }: ItineraryCardProps) {
  return (
    <article className="itinerary-card">
      <div className="day-badge">Day {day.day}</div>
      <div>
        <h3>{day.title}</h3>
        <ul>
          {day.activities.map((activity) => (
            <li key={activity}>{activity}</li>
          ))}
        </ul>
      </div>
    </article>
  );
}
