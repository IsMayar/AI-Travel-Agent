const API_URL = "http://localhost:8080/api/trips/plan";

export interface FlightOption {
  airline: string;
  price: number;
  duration: string;
}

export interface HotelOption {
  name: string;
  pricePerNight: number;
  rating: number;
}

export interface ItineraryDay {
  day: number;
  title: string;
  activities: string[];
}

export interface TripPlanResponse {
  destination: string;
  origin: string;
  budget: number;
  days: number;
  flightOptions: FlightOption[];
  hotelOptions: HotelOption[];
  itinerary: ItineraryDay[];
}

export async function planTrip(message: string): Promise<TripPlanResponse> {
  const response = await fetch(API_URL, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ message })
  });

  if (!response.ok) {
    throw new Error("Trip planning request failed.");
  }

  return response.json() as Promise<TripPlanResponse>;
}
