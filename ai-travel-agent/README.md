# AI Travel Agent

Full-stack AI Travel Agent app with a React + TypeScript + Vite frontend, a Spring Boot backend, and PostgreSQL persistence for saved trip plans.

## Backend Setup

Requirements:
- Java 17 or newer
- Maven
- PostgreSQL

Create a PostgreSQL database:

```powershell
createdb ai_travel_agent
```

If your local PostgreSQL user or password is different, set these environment variables before starting the backend:

```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/ai_travel_agent"
$env:POSTGRES_USER="postgres"
$env:POSTGRES_PASSWORD="postgres"
$env:OPENAI_API_KEY="your_openai_api_key_here"
```

Run the backend:

```powershell
cd backend
mvn spring-boot:run
```

Backend URLs:
- `GET http://localhost:8080/api/health`
- `POST http://localhost:8080/api/trips/plan`
- `POST http://localhost:8080/api/trips/save`
- `GET http://localhost:8080/api/trips`

Run backend tests:

```powershell
cd backend
mvn test
```

## Frontend Setup

Requirements:
- Node.js
- npm

Install dependencies:

```powershell
cd frontend
npm install
```

Run the frontend:

```powershell
npm run dev
```

Open:

```text
http://127.0.0.1:5173
```

Build the frontend:

```powershell
npm run build
```

Preview the production build:

```powershell
npm run preview
```

## Environment Files

Use `.env.example` as a reference. Real `.env` files are ignored by Git. Do not commit real API keys or secrets.
