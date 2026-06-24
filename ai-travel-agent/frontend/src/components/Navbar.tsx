import { useAppDispatch, useAppSelector } from "../app/hooks";
import { logout } from "../features/auth/authSlice";
import { api } from "../services/api";
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
  const dispatch = useAppDispatch();
  const { token, user } = useAppSelector((state) => state.auth);

  function handleLogout() {
    dispatch(logout());
    dispatch(api.util.resetApiState());
    window.location.href = "/login";
  }

  return (
    <header className="site-header">
      <Container className="nav-container">
        <a className="brand" href="/">
          <span className="brand-mark">AI</span>
          <span>Travel Agent</span>
        </a>
        {token ? (
          <>
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
            <div className="nav-auth">
              {user && <span>{user.fullName}</span>}
              <button
                className="secondary-button compact-button"
                onClick={handleLogout}
                type="button"
              >
                Logout
              </button>
            </div>
          </>
        ) : (
          <nav className="nav-links" aria-label="Authentication navigation">
            <a
              className={currentPath === "/login" ? "active" : undefined}
              href="/login"
            >
              Login
            </a>
            <a
              className={currentPath === "/register" ? "active" : undefined}
              href="/register"
            >
              Register
            </a>
          </nav>
        )}
      </Container>
    </header>
  );
}
