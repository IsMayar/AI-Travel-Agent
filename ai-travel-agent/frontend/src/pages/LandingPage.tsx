import { Container } from "../components/Container";
import { DestinationCard } from "../components/DestinationCard";
import { FeatureCard } from "../components/FeatureCard";
import { PromptChip } from "../components/PromptChip";
import { SectionTitle } from "../components/SectionTitle";
import { TripCard } from "../components/TripCard";
import {
  demoTripRecommendations,
  demoSavedTrips,
  featureCards,
  howItWorks,
  popularDestinations,
  promptSamples,
} from "../data/travelData";
import {
  useGetRecentTripsQuery,
  useGetTripRecommendationsQuery,
} from "../features/trips/tripsApi";

function openPlannerWithPrompt(prompt: string) {
  window.location.href = `/planner?prompt=${encodeURIComponent(prompt)}`;
}

export function LandingPage() {
  const recentTripsQuery = useGetRecentTripsQuery();
  const recommendationsQuery = useGetTripRecommendationsQuery();
  const recentTrips = recentTripsQuery.isError
    ? demoSavedTrips.slice(0, 5)
    : recentTripsQuery.data ?? [];
  const recommendations = recommendationsQuery.isError
    ? demoTripRecommendations
    : recommendationsQuery.data?.recommendations ?? [];

  return (
    <main>
      <section className="hero-section">
        <Container className="hero-content">
          <p className="eyebrow">AI-powered travel planning</p>
          <h1>Plan your perfect trip with AI</h1>
          <p>
            Turn a plain-language travel request into a clean mock trip plan with
            budget, flights, hotels, itinerary ideas, and saved trip history.
          </p>
          <div className="hero-actions">
            <a className="primary-button" href="/planner">
              Start Planning
            </a>
            <a className="secondary-button hero-secondary" href="/saved-trips">
              View Saved Trips
            </a>
          </div>
          <div className="prompt-row hero-prompts" aria-label="Example prompts">
            {promptSamples.slice(0, 3).map((prompt) => (
              <PromptChip key={prompt} onClick={() => openPlannerWithPrompt(prompt)}>
                {prompt}
              </PromptChip>
            ))}
          </div>
        </Container>
      </section>

      <section className="page-section">
        <Container>
          <SectionTitle
            eyebrow="Recent trips"
            title="Fresh plans from your saved trips"
            description="Jump back into the newest saved mock trips from the backend."
          />

          {recentTripsQuery.isLoading && (
            <div className="saved-trip-grid" aria-label="Loading recent trips">
              <div className="skeleton-card" />
              <div className="skeleton-card" />
              <div className="skeleton-card" />
            </div>
          )}

          {!recentTripsQuery.isLoading && recentTripsQuery.isError && (
            <p className="recent-trip-note">
              Backend recent trips are unavailable, so demo trips are shown for now.
            </p>
          )}

          {!recentTripsQuery.isLoading && recentTrips.length === 0 && (
            <div className="recent-trip-empty">
              <p>No saved trips yet.</p>
              <a className="secondary-button" href="/planner">
                Plan a Trip
              </a>
            </div>
          )}

          {!recentTripsQuery.isLoading && recentTrips.length > 0 && (
            <div className="saved-trip-grid">
              {recentTrips.map((trip) => (
                <TripCard href={`/trips/${trip.id}`} key={trip.id} trip={trip} />
              ))}
            </div>
          )}
        </Container>
      </section>

      <section className="page-section tinted-section">
        <Container>
          <SectionTitle
            eyebrow="Recommended trips"
            title="Three mock ideas for your next plan"
            description="Mock recommendations shaped by your saved trips and travel preferences when available."
          />

          {recommendationsQuery.isLoading && (
            <div className="recommendation-grid" aria-label="Loading recommendations">
              <div className="skeleton-card" />
              <div className="skeleton-card" />
              <div className="skeleton-card" />
            </div>
          )}

          {!recommendationsQuery.isLoading && recommendationsQuery.isError && (
            <p className="recent-trip-note">
              Backend recommendations are unavailable, so demo ideas are shown for now.
            </p>
          )}

          {!recommendationsQuery.isLoading && recommendations.length > 0 && (
            <div className="recommendation-grid">
              {recommendations.map((recommendation) => (
                <article
                  className="recommendation-card"
                  key={`${recommendation.origin}-${recommendation.destination}`}
                >
                  <p className="eyebrow">{recommendation.travelStyle}</p>
                  <h3>
                    {recommendation.origin} to {recommendation.destination}
                  </h3>
                  <p>{recommendation.reason}</p>
                  <div className="trip-card-meta">
                    <span>{recommendation.days} days</span>
                    <span>${recommendation.budget.toLocaleString()}</span>
                  </div>
                </article>
              ))}
            </div>
          )}
        </Container>
      </section>

      <section className="page-section">
        <Container>
          <SectionTitle
            eyebrow="Popular destinations"
            title="Start with somewhere inspiring"
            description="Use these mock destination ideas as a fast starting point for the MVP planner."
          />
          <div className="destination-grid">
            {popularDestinations.map((destination) => (
              <DestinationCard
                description={destination.description}
                imageUrl={destination.imageUrl}
                key={destination.name}
                name={destination.name}
              />
            ))}
          </div>
        </Container>
      </section>

      <section className="page-section tinted-section">
        <Container>
          <SectionTitle
            eyebrow="How it works"
            title="From idea to itinerary in three steps"
            description="The MVP uses backend mock data today, with space for real providers later."
          />
          <div className="feature-grid">
            {howItWorks.map((feature) => (
              <FeatureCard
                description={feature.description}
                key={feature.title}
                title={feature.title}
              />
            ))}
          </div>
        </Container>
      </section>

      <section className="page-section">
        <Container>
          <SectionTitle
            eyebrow="Why use AI Travel Agent"
            title="A calmer way to shape a trip"
            description="Keep the planning surface focused, readable, and ready for the next backend capabilities."
          />
          <div className="feature-grid">
            {featureCards.map((feature) => (
              <FeatureCard
                description={feature.description}
                key={feature.title}
                title={feature.title}
              />
            ))}
          </div>
        </Container>
      </section>
    </main>
  );
}
