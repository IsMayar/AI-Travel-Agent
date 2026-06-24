import { type FormEvent, useState } from "react";

import { useAppDispatch } from "../app/hooks";
import { Container } from "../components/Container";
import { useLoginMutation } from "../features/auth/authApi";
import { setCredentials } from "../features/auth/authSlice";

export function LoginPage() {
  const dispatch = useAppDispatch();
  const [login, { isLoading }] = useLoginMutation();
  const [email, setEmail] = useState("test@example.com");
  const [password, setPassword] = useState("Password@123");
  const [error, setError] = useState("");

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!email.trim() || !password.trim()) {
      return;
    }

    setError("");

    try {
      const response = await login({
        email: email.trim(),
        password,
      }).unwrap();
      dispatch(setCredentials(response));
      window.location.href = "/";
    } catch {
      setError("Could not sign in. Check your email and password.");
    }
  }

  return (
    <main className="auth-page">
      <Container className="auth-container">
        <section className="auth-card" aria-labelledby="login-title">
          <p className="eyebrow">Welcome back</p>
          <h1 id="login-title">Sign in to AI Travel Agent</h1>
          <p>Use your account to manage saved trips and planning data.</p>

          <form className="auth-form" onSubmit={handleSubmit}>
            <label htmlFor="login-email">
              Email
              <input
                autoComplete="email"
                id="login-email"
                onChange={(event) => setEmail(event.target.value)}
                type="email"
                value={email}
              />
            </label>
            <label htmlFor="login-password">
              Password
              <input
                autoComplete="current-password"
                id="login-password"
                onChange={(event) => setPassword(event.target.value)}
                type="password"
                value={password}
              />
            </label>

            {error && <p className="preferences-message error-text">{error}</p>}

            <button
              className="primary-button"
              disabled={isLoading || !email.trim() || !password.trim()}
              type="submit"
            >
              {isLoading ? "Signing in..." : "Sign In"}
            </button>
          </form>

          <p className="auth-switch">
            New here? <a href="/register">Create an account</a>
          </p>
        </section>
      </Container>
    </main>
  );
}
