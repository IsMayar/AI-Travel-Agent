import { useEffect } from "react";

import { useAppDispatch, useAppSelector } from "./app/hooks";
import { Layout } from "./components/Layout";
import { useGetMeQuery } from "./features/auth/authApi";
import { logout, setUser } from "./features/auth/authSlice";
import { DashboardPage } from "./pages/DashboardPage";
import { LandingPage } from "./pages/LandingPage";
import { LoginPage } from "./pages/LoginPage";
import { NotFoundPage } from "./pages/NotFoundPage";
import { PreferencesPage } from "./pages/PreferencesPage";
import { RegisterPage } from "./pages/RegisterPage";
import { SavedTripsPage } from "./pages/SavedTripsPage";
import { TripDetailsPage } from "./pages/TripDetailsPage";
import { TripPlannerPage } from "./pages/TripPlannerPage";

function normalizePath(pathname: string) {
  if (pathname.length > 1 && pathname.endsWith("/")) {
    return pathname.slice(0, -1);
  }

  return pathname;
}

function renderPage(path: string) {
  if (path === "/") {
    return <LandingPage />;
  }

  if (path === "/login") {
    return <LoginPage />;
  }

  if (path === "/register") {
    return <RegisterPage />;
  }

  if (path === "/planner") {
    return <TripPlannerPage />;
  }

  if (path === "/saved-trips") {
    return <SavedTripsPage />;
  }

  if (path === "/dashboard") {
    return <DashboardPage />;
  }

  if (path === "/preferences") {
    return <PreferencesPage />;
  }

  const tripDetailsMatch = path.match(/^\/trips\/([^/]+)$/);
  if (tripDetailsMatch) {
    return <TripDetailsPage tripIdParam={tripDetailsMatch[1]} />;
  }

  return <NotFoundPage />;
}

export function App() {
  const dispatch = useAppDispatch();
  const { token } = useAppSelector((state) => state.auth);
  const meQuery = useGetMeQuery(undefined, { skip: !token });
  const path = normalizePath(window.location.pathname);
  const isAuthRoute = path === "/login" || path === "/register";

  useEffect(() => {
    if (meQuery.data) {
      dispatch(setUser(meQuery.data));
    }
  }, [dispatch, meQuery.data]);

  useEffect(() => {
    if (meQuery.isError) {
      dispatch(logout());
      window.history.replaceState(null, "", "/login");
    }
  }, [dispatch, meQuery.isError]);

  if (!token && !isAuthRoute) {
    window.history.replaceState(null, "", "/login");
    return (
      <Layout>
        <LoginPage />
      </Layout>
    );
  }

  if (token && isAuthRoute) {
    window.history.replaceState(null, "", "/");
    return <Layout>{renderPage("/")}</Layout>;
  }

  return <Layout>{renderPage(path)}</Layout>;
}
