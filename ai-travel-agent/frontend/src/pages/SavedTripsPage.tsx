import { Container } from "../components/Container";
import { EmptyState } from "../components/EmptyState";
import { ErrorState } from "../components/ErrorState";
import { SectionTitle } from "../components/SectionTitle";
import { TripCard } from "../components/TripCard";
import { demoSavedTrips } from "../data/travelData";
import { useGetSavedTripsQuery } from "../features/trips/tripsApi";

function SavedTripsSkeleton() {
  return (
    <div className="saved-trip-grid" aria-label="Loading saved trips">
      <div className="skeleton-card" />
      <div className="skeleton-card" />
      <div className="skeleton-card" />
    </div>
  );
}

export function SavedTripsPage() {
  const { data, isError, isLoading, refetch } = useGetSavedTripsQuery();
  const trips = isError ? demoSavedTrips : data ?? [];

  return (
    <main className="simple-page">
      <Container>
        <SectionTitle
          eyebrow="Saved trips"
          title="Your trip ideas"
          description="Saved backend trips appear here. If the backend is offline, the page shows demo trips instead."
        />

        {isLoading && <SavedTripsSkeleton />}

        {!isLoading && isError && (
          <ErrorState
            actionLabel="Try Again"
            message="Backend saved trips are unavailable, so demo trips are shown for now."
            onAction={() => {
              void refetch();
            }}
            title="Could not load saved trips"
          />
        )}

        {!isLoading && trips.length === 0 && (
          <EmptyState
            actionHref="/planner"
            actionLabel="Plan a Trip"
            message="Create and save a trip plan to build your travel shortlist."
            title="No saved trips yet"
          />
        )}

        {!isLoading && trips.length > 0 && (
          <div className="saved-trip-grid">
            {trips.map((trip) => (
              <TripCard key={trip.id} trip={trip} />
            ))}
          </div>
        )}
      </Container>
    </main>
  );
}
