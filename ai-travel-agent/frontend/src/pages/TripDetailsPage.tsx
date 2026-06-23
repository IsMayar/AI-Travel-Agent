import { skipToken } from "@reduxjs/toolkit/query";

import { Container } from "../components/Container";
import { EmptyState } from "../components/EmptyState";
import { ErrorState } from "../components/ErrorState";
import { SectionTitle } from "../components/SectionTitle";
import { useGetSavedTripQuery } from "../features/trips/tripsApi";

interface TripDetailsPageProps {
  tripIdParam: string;
}

function parseTripId(value: string) {
  const id = Number(value);

  if (!Number.isInteger(id) || id <= 0) {
    return null;
  }

  return id;
}

function formatDate(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "Unknown";
  }

  return new Intl.DateTimeFormat("en", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

function TripDetailsSkeleton() {
  return (
    <div className="trip-details-card" aria-label="Loading trip details">
      <div className="skeleton-card large" />
    </div>
  );
}

export function TripDetailsPage({ tripIdParam }: TripDetailsPageProps) {
  const tripId = parseTripId(tripIdParam);
  const { data: trip, isError, isLoading } = useGetSavedTripQuery(
    tripId ?? skipToken
  );

  return (
    <main className="simple-page">
      <Container>
        <a className="back-link" href="/saved-trips">
          Back to Saved Trips
        </a>

        <SectionTitle
          eyebrow="Trip details"
          title="Saved trip overview"
          description="Review the basic saved trip information stored by the MVP backend."
        />

        {!tripId && (
          <EmptyState
            actionHref="/saved-trips"
            actionLabel="View Saved Trips"
            message="This trip link is missing a valid saved trip id."
            title="No trip selected"
          />
        )}

        {tripId && isLoading && <TripDetailsSkeleton />}

        {tripId && isError && (
          <ErrorState
            actionLabel="Back to Saved Trips"
            message="This saved trip could not be found or the backend is unavailable."
            onAction={() => {
              window.location.href = "/saved-trips";
            }}
            title="Could not load trip"
          />
        )}

        {tripId && !isLoading && !isError && !trip && (
          <EmptyState
            actionHref="/saved-trips"
            actionLabel="View Saved Trips"
            message="There is no saved trip data to display."
            title="Trip details are empty"
          />
        )}

        {trip && (
          <article className="trip-details-card">
            <div className="trip-details-hero">
              <p className="eyebrow">Destination</p>
              <h3>{trip.destination}</h3>
              <p>{trip.userMessage}</p>
            </div>

            <div className="details-grid">
              <div>
                <span>Origin</span>
                <strong>{trip.origin}</strong>
              </div>
              <div>
                <span>Destination</span>
                <strong>{trip.destination}</strong>
              </div>
              <div>
                <span>Budget</span>
                <strong>${trip.budget.toLocaleString()}</strong>
              </div>
              <div>
                <span>Days</span>
                <strong>{trip.days}</strong>
              </div>
              <div>
                <span>Created</span>
                <strong>{formatDate(trip.createdAt)}</strong>
              </div>
            </div>
          </article>
        )}
      </Container>
    </main>
  );
}
