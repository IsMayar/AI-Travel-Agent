import { Container } from "./Container";

export function Footer() {
  return (
    <footer className="site-footer">
      <Container className="footer-grid">
        <div>
          <strong>AI Travel Agent</strong>
          <p>Mock-powered trip planning for the MVP build.</p>
        </div>
        <div className="footer-links">
          <a href="/planner">Planner</a>
          <a href="/saved-trips">Saved Trips</a>
        </div>
      </Container>
    </footer>
  );
}
