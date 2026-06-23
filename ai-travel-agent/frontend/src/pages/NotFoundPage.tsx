import { Container } from "../components/Container";
import { EmptyState } from "../components/EmptyState";

export function NotFoundPage() {
  return (
    <main className="simple-page">
      <Container>
        <EmptyState
          actionHref="/"
          actionLabel="Go Home"
          message="The page you are looking for is not part of the AI Travel Agent MVP."
          title="404 - Page not found"
        />
      </Container>
    </main>
  );
}
