import { Layout } from "./components/Layout";
import { LandingPage } from "./pages/LandingPage";
import { NotFoundPage } from "./pages/NotFoundPage";
import { SavedTripsPage } from "./pages/SavedTripsPage";
import { TripPlannerPage } from "./pages/TripPlannerPage";

function normalizePath(pathname: string) {
  if (pathname.length > 1 && pathname.endsWith("/")) {
    return pathname.slice(0, -1);
  }

  return pathname;
}

function renderPage() {
  const path = normalizePath(window.location.pathname);

  if (path === "/") {
    return <LandingPage />;
  }

  if (path === "/planner") {
    return <TripPlannerPage />;
  }

  if (path === "/saved-trips") {
    return <SavedTripsPage />;
  }

  return <NotFoundPage />;
}

export function App() {
  return <Layout>{renderPage()}</Layout>;
}
