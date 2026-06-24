import { Container } from "./Container";

const navItems = [
  { label: "Home", href: "/" },
  { label: "Planner", href: "/planner" },
  { label: "Saved Trips", href: "/saved-trips" },
  { label: "Dashboard", href: "/dashboard" },
  { label: "Preferences", href: "/preferences" },
];

export function Navbar() {
  const currentPath = window.location.pathname;

  return (
    <header className="site-header">
      <Container className="nav-container">
        <a className="brand" href="/">
          <span className="brand-mark">AI</span>
          <span>Travel Agent</span>
        </a>
        <nav className="nav-links" aria-label="Primary navigation">
          {navItems.map((item) => (
            <a
              className={currentPath === item.href ? "active" : undefined}
              href={item.href}
              key={item.href}
            >
              {item.label}
            </a>
          ))}
        </nav>
        <a className="nav-cta" href="/planner">
          Plan Trip
        </a>
      </Container>
    </header>
  );
}
