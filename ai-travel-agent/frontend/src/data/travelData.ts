import type {
  SavedTrip,
  TripPlanResponse,
} from "../features/trips/tripsApi";

export const promptSamples = [
  "Plan a 7-day trip from Austin to Dubai under $1500",
  "Find a relaxing 5-day beach trip from Miami under $1200",
  "Create a 4-day Tokyo food and culture itinerary",
  "Plan a family-friendly Paris trip for spring break",
];

export const demoTripPlan: TripPlanResponse = {
  origin: "Austin",
  destination: "Dubai",
  budget: 1500,
  days: 7,
  flightOptions: [
    {
      airline: "Mock Airline",
      price: 850,
      duration: "18h 30m",
    },
    {
      airline: "Demo Air",
      price: 910,
      duration: "20h 10m",
    },
  ],
  hotelOptions: [
    {
      name: "Mock Hotel Dubai",
      pricePerNight: 90,
      rating: 4.3,
    },
    {
      name: "Creekside Demo Suites",
      pricePerNight: 112,
      rating: 4.5,
    },
  ],
  itinerary: [
    {
      day: 1,
      title: "Arrival and hotel check-in",
      activities: ["Arrive in Dubai", "Check into hotel", "Evening walk"],
    },
    {
      day: 2,
      title: "Old Dubai and creek views",
      activities: ["Visit Al Fahidi", "Ride an abra", "Explore spice souks"],
    },
    {
      day: 3,
      title: "Modern skyline day",
      activities: ["Burj Khalifa viewpoint", "Dubai Mall", "Fountain show"],
    },
  ],
};

export const demoSavedTrips: SavedTrip[] = [
  {
    id: 101,
    userMessage: "Plan a 7-day trip from Austin to Dubai under $1500",
    origin: "Austin",
    destination: "Dubai",
    budget: 1500,
    days: 7,
    favorite: true,
    createdAt: "2026-06-21T06:13:11.653Z",
    updatedAt: "2026-06-21T06:13:11.653Z",
  },
  {
    id: 102,
    userMessage: "Create a 4-day Tokyo food and culture itinerary",
    origin: "Seattle",
    destination: "Tokyo",
    budget: 2200,
    days: 4,
    favorite: false,
    createdAt: "2026-06-20T09:45:00.000Z",
    updatedAt: "2026-06-20T09:45:00.000Z",
  },
];

export const travelTips = [
  "Book airport transfers early if your flight lands late at night.",
  "Keep one flexible afternoon for weather, rest, or a spontaneous local find.",
  "Compare hotel neighborhoods against transit time, not only nightly price.",
];

export const recommendedActivities = [
  "Sunset viewpoint",
  "Local market walk",
  "Guided history tour",
  "Signature dinner reservation",
];

export const featureCards = [
  {
    title: "Budget-aware planning",
    description:
      "Turn a rough travel idea into flights, hotels, and daily pacing that respect your target spend.",
  },
  {
    title: "Fast itinerary drafts",
    description:
      "Start from a clean first version instead of stitching together notes across tabs.",
  },
  {
    title: "Saved trip history",
    description:
      "Keep the basic plans you liked and return to them when you are ready to refine.",
  },
];

export const howItWorks = [
  {
    title: "Describe the trip",
    description:
      "Share origin, destination, budget, dates, or travel style in plain language.",
  },
  {
    title: "Review a draft",
    description:
      "Get mock flight and hotel options plus a day-by-day itinerary layout.",
  },
  {
    title: "Save the plan",
    description:
      "Store the core trip details so your best ideas do not disappear.",
  },
];

export const popularDestinations = [
  {
    name: "Dubai",
    description: "Skyline views, desert evenings, and modern waterfront stays.",
    imageUrl:
      "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=900&q=80",
  },
  {
    name: "Tokyo",
    description: "Food alleys, quiet temples, bright districts, and rail-perfect days.",
    imageUrl:
      "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?auto=format&fit=crop&w=900&q=80",
  },
  {
    name: "Paris",
    description: "Classic museums, neighborhood cafes, river walks, and easy day trips.",
    imageUrl:
      "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=900&q=80",
  },
];
