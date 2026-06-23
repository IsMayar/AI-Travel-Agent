import { useState } from "react";

import { Container } from "../components/Container";
import { EmptyState } from "../components/EmptyState";
import { ErrorState } from "../components/ErrorState";
import { SectionTitle } from "../components/SectionTitle";
import { TripCard } from "../components/TripCard";
import { demoSavedTrips } from "../data/travelData";
import {
  SavedTrip,
  useDeleteSavedTripMutation,
  useGetSavedTripsQuery,
} from "../features/trips/tripsApi";

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
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [deletingTripId, setDeletingTripId] = useState<number | null>(null);
  const { data, isError, isLoading, refetch } = useGetSavedTripsQuery();
  const [deleteSavedTrip] = useDeleteSavedTripMutation();
  const trips = isError ? demoSavedTrips : data ?? [];

  async function handleDeleteTrip(trip: SavedTrip) {
    const confirmed = window.confirm(
      `Delete the saved trip from ${trip.origin} to ${trip.destination}?`
    );

    if (!confirmed) {
      return;
    }

    setDeleteError(null);
    setDeletingTripId(trip.id);

    try {
      await deleteSavedTrip(trip.id).unwrap();
    } catch {
      setDeleteError("Could not delete this saved trip. Please try again.");
    } finally {
      setDeletingTripId(null);
    }
  }

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

        {!isLoading && deleteError && (
          <ErrorState title="Delete failed" message={deleteError} />
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
              <TripCard
                href={`/trips/${trip.id}`}
                isDeleting={deletingTripId === trip.id}
                key={trip.id}
                onDelete={handleDeleteTrip}
                trip={trip}
              />
            ))}
          </div>
        )}
      </Container>
    </main>
  );
}
