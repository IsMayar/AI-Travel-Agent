import { useState } from "react";

import { Container } from "../components/Container";
import { EditTripModal } from "../components/EditTripModal";
import { EmptyState } from "../components/EmptyState";
import { ErrorState } from "../components/ErrorState";
import { SectionTitle } from "../components/SectionTitle";
import { TripCard } from "../components/TripCard";
import { demoSavedTrips } from "../data/travelData";
import {
  SaveTripRequest,
  SavedTrip,
  useDuplicateSavedTripMutation,
  useDeleteSavedTripMutation,
  useGetSavedTripsQuery,
  useSearchSavedTripsQuery,
  useToggleFavoriteTripMutation,
  useUpdateSavedTripMutation,
} from "../features/trips/tripsApi";

type TripAction = "delete" | "duplicate" | "favorite";

interface ActiveAction {
  id: number;
  type: TripAction;
}

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
  const [actionError, setActionError] = useState<string | null>(null);
  const [activeAction, setActiveAction] = useState<ActiveAction | null>(null);
  const [editingTrip, setEditingTrip] = useState<SavedTrip | null>(null);
  const [favoriteOnly, setFavoriteOnly] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const trimmedSearch = searchTerm.trim();
  const savedTripsQuery = useGetSavedTripsQuery(
    favoriteOnly ? { favorite: true } : undefined,
    { skip: trimmedSearch.length > 0 }
  );
  const searchTripsQuery = useSearchSavedTripsQuery(trimmedSearch, {
    skip: trimmedSearch.length === 0,
  });
  const [deleteSavedTrip] = useDeleteSavedTripMutation();
  const [duplicateSavedTrip] = useDuplicateSavedTripMutation();
  const [toggleFavoriteTrip] = useToggleFavoriteTripMutation();
  const [updateSavedTrip, { isLoading: isUpdating }] =
    useUpdateSavedTripMutation();
  const activeQuery = trimmedSearch ? searchTripsQuery : savedTripsQuery;
  const serverTrips = activeQuery.data ?? [];
  const fallbackTrips = demoSavedTrips.filter((trip) => {
    const matchesSearch =
      !trimmedSearch ||
      `${trip.destination} ${trip.origin} ${trip.userMessage}`
        .toLowerCase()
        .includes(trimmedSearch.toLowerCase());
    const matchesFavorite = !favoriteOnly || trip.favorite;

    return matchesSearch && matchesFavorite;
  });
  const trips = activeQuery.isError
    ? fallbackTrips
    : favoriteOnly && trimmedSearch
      ? serverTrips.filter((trip) => trip.favorite)
      : serverTrips;

  function isActionLoading(trip: SavedTrip, type: TripAction) {
    return activeAction?.id === trip.id && activeAction.type === type;
  }

  function handleRefetch() {
    void activeQuery.refetch();
  }

  async function handleDeleteTrip(trip: SavedTrip) {
    const confirmed = window.confirm(
      `Delete the saved trip from ${trip.origin} to ${trip.destination}?`
    );

    if (!confirmed) {
      return;
    }

    setActionError(null);
    setActiveAction({ id: trip.id, type: "delete" });

    try {
      await deleteSavedTrip(trip.id).unwrap();
    } catch {
      setActionError("Could not delete this saved trip. Please try again.");
    } finally {
      setActiveAction(null);
    }
  }

  async function handleToggleFavorite(trip: SavedTrip) {
    setActionError(null);
    setActiveAction({ id: trip.id, type: "favorite" });

    try {
      await toggleFavoriteTrip(trip.id).unwrap();
    } catch {
      setActionError("Could not update favorite status. Please try again.");
    } finally {
      setActiveAction(null);
    }
  }

  async function handleDuplicateTrip(trip: SavedTrip) {
    setActionError(null);
    setActiveAction({ id: trip.id, type: "duplicate" });

    try {
      await duplicateSavedTrip(trip.id).unwrap();
    } catch {
      setActionError("Could not duplicate this saved trip. Please try again.");
    } finally {
      setActiveAction(null);
    }
  }

  async function handleUpdateTrip(trip: SavedTrip, values: SaveTripRequest) {
    setActionError(null);

    try {
      await updateSavedTrip({ id: trip.id, trip: values }).unwrap();
      setEditingTrip(null);
    } catch {
      setActionError("Could not update this saved trip. Please try again.");
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

        <div className="saved-toolbar">
          <label className="search-field">
            Search trips
            <input
              onChange={(event) => setSearchTerm(event.target.value)}
              placeholder="Search by destination, origin, or request"
              type="search"
              value={searchTerm}
            />
          </label>
          <label className="favorite-toggle">
            <input
              checked={favoriteOnly}
              onChange={(event) => setFavoriteOnly(event.target.checked)}
              type="checkbox"
            />
            Favorites only
          </label>
        </div>

        {activeQuery.isLoading && <SavedTripsSkeleton />}

        {!activeQuery.isLoading && activeQuery.isError && (
          <ErrorState
            actionLabel="Try Again"
            message="Backend saved trips are unavailable, so demo trips are shown for now."
            onAction={handleRefetch}
            title="Could not load saved trips"
          />
        )}

        {!activeQuery.isLoading && actionError && (
          <ErrorState title="Trip action failed" message={actionError} />
        )}

        {!activeQuery.isLoading && trips.length === 0 && (
          <EmptyState
            actionHref="/planner"
            actionLabel="Plan a Trip"
            message={
              trimmedSearch || favoriteOnly
                ? "Try a different search or turn off the favorite filter."
                : "Create and save a trip plan to build your travel shortlist."
            }
            title={
              trimmedSearch || favoriteOnly
                ? "No trips match your filters"
                : "No saved trips yet"
            }
          />
        )}

        {!activeQuery.isLoading && trips.length > 0 && (
          <div className="saved-trip-grid">
            {trips.map((trip) => (
              <TripCard
                href={`/trips/${trip.id}`}
                isDeleting={isActionLoading(trip, "delete")}
                isDuplicating={isActionLoading(trip, "duplicate")}
                isFavoriting={isActionLoading(trip, "favorite")}
                key={trip.id}
                onDelete={handleDeleteTrip}
                onDuplicate={handleDuplicateTrip}
                onEdit={setEditingTrip}
                onToggleFavorite={handleToggleFavorite}
                trip={trip}
              />
            ))}
          </div>
        )}

        <EditTripModal
          isSaving={isUpdating}
          onClose={() => setEditingTrip(null)}
          onSave={handleUpdateTrip}
          trip={editingTrip}
        />
      </Container>
    </main>
  );
}
