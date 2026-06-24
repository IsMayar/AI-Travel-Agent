import { type FormEvent, useState } from "react";

import { useAppDispatch } from "../app/hooks";
import { Container } from "../components/Container";
import { useRegisterMutation } from "../features/auth/authApi";
import { setCredentials } from "../features/auth/authSlice";

export function RegisterPage() {
  const dispatch = useAppDispatch();
  const [register, { isLoading }] = useRegisterMutation();
  const [fullName, setFullName] = useState("Faisal Mayar");
  const [email, setEmail] = useState("test@example.com");
  const [password, setPassword] = useState("Password@123");
  const [error, setError] = useState("");

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!fullName.trim() || !email.trim() || !password.trim()) {
      return;
    }

    setError("");

    try {
      const response = await register({
        email: email.trim(),
        fullName: fullName.trim(),
        password,
      }).unwrap();
      dispatch(setCredentials(response));
      window.location.href = "/";
    } catch {
      setError("Could not create this account. The email may already be registered.");
    }
  }

  return (
    <main className="auth-page">
      <Container className="auth-container">
        <section className="auth-card" aria-labelledby="register-title">
          <p className="eyebrow">Create account</p>
          <h1 id="register-title">Start planning with AI Travel Agent</h1>
          <p>Register once, then keep your mock trip workspace protected.</p>

          <form className="auth-form" onSubmit={handleSubmit}>
            <label htmlFor="register-full-name">
              Full name
              <input
                autoComplete="name"
                id="register-full-name"
                onChange={(event) => setFullName(event.target.value)}
                type="text"
                value={fullName}
              />
            </label>
            <label htmlFor="register-email">
              Email
              <input
                autoComplete="email"
                id="register-email"
                onChange={(event) => setEmail(event.target.value)}
                type="email"
                value={email}
              />
            </label>
            <label htmlFor="register-password">
              Password
              <input
                autoComplete="new-password"
                id="register-password"
                onChange={(event) => setPassword(event.target.value)}
                type="password"
                value={password}
              />
            </label>

            {error && <p className="preferences-message error-text">{error}</p>}

            <button
              className="primary-button"
              disabled={
                isLoading ||
                !fullName.trim() ||
                !email.trim() ||
                !password.trim()
              }
              type="submit"
            >
              {isLoading ? "Creating..." : "Create Account"}
            </button>
          </form>

          <p className="auth-switch">
            Already registered? <a href="/login">Sign in</a>
          </p>
        </section>
      </Container>
    </main>
  );
}
