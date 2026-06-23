import { Container } from "../components/Container";
import { DestinationCard } from "../components/DestinationCard";
import { FeatureCard } from "../components/FeatureCard";
import { PromptChip } from "../components/PromptChip";
import { SectionTitle } from "../components/SectionTitle";
import {
  featureCards,
  howItWorks,
  popularDestinations,
  promptSamples,
} from "../data/travelData";

function openPlannerWithPrompt(prompt: string) {
  window.location.href = `/planner?prompt=${encodeURIComponent(prompt)}`;
}

export function LandingPage() {
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
