import { Container } from "../components/Container";
import { EmptyState } from "../components/EmptyState";
import { ErrorState } from "../components/ErrorState";
import { SectionTitle } from "../components/SectionTitle";
import { TripCard } from "../components/TripCard";
import { useGetDashboardSummaryQuery } from "../features/trips/tripsApi";

const currencyFormatter = new Intl.NumberFormat("en", {
  currency: "USD",
  maximumFractionDigits: 2,
  style: "currency",
});

function DashboardSkeleton() {
  return (
    <>
      <div className="dashboard-summary-grid" aria-label="Loading dashboard">
        <div className="stats-card loading-stat" />
        <div className="stats-card loading-stat" />
        <div className="stats-card loading-stat" />
        <div className="stats-card loading-stat" />
      </div>
      <div className="saved-trip-grid">
        <div className="skeleton-card" />
        <div className="skeleton-card" />
        <div className="skeleton-card" />
      </div>
    </>
  );
}

export function DashboardPage() {
  const { data: summary, isError, isLoading, refetch } =
    useGetDashboardSummaryQuery();

  return (
    <main className="simple-page">
      <Container>
        <SectionTitle
          eyebrow="Dashboard"
          title="Travel workspace summary"
          description="A quick view of saved trips and the local planning data connected to them."
        />

        {isLoading && <DashboardSkeleton />}

        {!isLoading && isError && (
          <ErrorState
            actionLabel="Try Again"
            message="The dashboard summary is unavailable right now."
            onAction={() => void refetch()}
            title="Could not load dashboard"
          />
        )}

        {!isLoading && !isError && !summary && (
          <EmptyState
            actionHref="/planner"
            actionLabel="Plan a Trip"
            message="Dashboard data will appear after you save trips."
            title="No dashboard data"
          />
        )}

        {!isLoading && !isError && summary && (
          <>
            <div className="dashboard-summary-grid">
              <div className="stats-card">
                <span>Total trips</span>
                <strong>{summary.totalTrips.toLocaleString()}</strong>
              </div>
              <div className="stats-card">
                <span>Favorites</span>
                <strong>{summary.favoriteTrips.toLocaleString()}</strong>
              </div>
              <div className="stats-card">
                <span>Notes</span>
                <strong>{summary.totalNotes.toLocaleString()}</strong>
              </div>
              <div className="stats-card">
                <span>Checklist items</span>
                <strong>
                  {summary.completedChecklistItems.toLocaleString()} /{" "}
                  {summary.totalChecklistItems.toLocaleString()}
                </strong>
              </div>
              <div className="stats-card">
                <span>Documents</span>
                <strong>{summary.totalDocuments.toLocaleString()}</strong>
              </div>
              <div className="stats-card">
                <span>Budget total</span>
                <strong>
                  {currencyFormatter.format(summary.totalBudgetAmount)}
                </strong>
              </div>
              <div className="stats-card">
                <span>Itinerary items</span>
                <strong>{summary.totalItineraryItems.toLocaleString()}</strong>
              </div>
              <div className="stats-card">
                <span>Tags</span>
                <strong>{summary.totalTags.toLocaleString()}</strong>
              </div>
            </div>

            <section className="dashboard-recent-section">
              <SectionTitle
                eyebrow="Recent"
                title="Recently saved trips"
                description="The five newest trips from the backend."
              />

              {summary.recentTrips.length === 0 ? (
                <EmptyState
                  actionHref="/planner"
                  actionLabel="Plan a Trip"
                  message="Save a trip plan to populate the recent trips dashboard."
                  title="No recent trips"
                />
              ) : (
                <div className="saved-trip-grid">
                  {summary.recentTrips.map((trip) => (
                    <TripCard
                      href={`/trips/${trip.id}`}
                      key={trip.id}
                      trip={trip}
                    />
                  ))}
                </div>
              )}
            </section>
          </>
        )}
      </Container>
    </main>
  );
}
