import { Layout } from "./components/Layout";
import { LandingPage } from "./pages/LandingPage";
import { NotFoundPage } from "./pages/NotFoundPage";
import { SavedTripsPage } from "./pages/SavedTripsPage";
import { TripDetailsPage } from "./pages/TripDetailsPage";
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

  const tripDetailsMatch = path.match(/^\/trips\/([^/]+)$/);
  if (tripDetailsMatch) {
    return <TripDetailsPage tripIdParam={tripDetailsMatch[1]} />;
  }

  return <NotFoundPage />;
}

export function App() {
  return <Layout>{renderPage()}</Layout>;
}
