import type { HotelOption } from "../features/trips/tripsApi";

interface HotelCardProps {
  hotel: HotelOption;
}

export function HotelCard({ hotel }: HotelCardProps) {
  return (
    <article className="option-card">
      <div>
        <p className="eyebrow">Hotel</p>
        <h3>{hotel.name}</h3>
      </div>
      <div className="option-meta">
        <span>${hotel.pricePerNight}/night</span>
        <span>{hotel.rating}/5 rating</span>
      </div>
    </article>
  );
}
