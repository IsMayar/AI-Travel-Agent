# AI Travel Agent

Full-stack MVP for an AI Travel Agent concept.

The app uses:
- React + TypeScript + Vite frontend
- Spring Boot Java backend
- PostgreSQL for saved trip plans
- Mock trip-planning data only

OpenAI, real travel APIs, authentication, payments, and booking are not added yet.

## Prerequisites

- Java 17 or newer
- Maven
- Node.js and npm
- PostgreSQL

## PostgreSQL Database Setup

Create the local database:

```powershell
createdb ai_travel_agent
```

Set database connection values before starting the backend:

```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/ai_travel_agent"
$env:POSTGRES_USER="postgres"
$env:POSTGRES_PASSWORD="postgres"
```

If your PostgreSQL user, password, host, or database name is different, update those values for your machine.

## Backend Startup

```powershell
cd backend
mvn spring-boot:run
```

The backend runs at:

```text
http://localhost:8080
```

Run backend tests and package:

```powershell
cd backend
mvn package
```

## Frontend Startup

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

## Available Endpoints

### Health

```http
GET /api/health
```

Returns:

```json
{
  "status": "ok",
  "app": "ai-travel-agent"
}
```

### Plan Trip

```http
POST /api/trips/plan
```

Request:

```json
{
  "message": "Plan a 7-day trip from Austin to Dubai under $1500"
}
```

Returns a mock trip plan with origin, destination, budget, days, flight options, hotel options, and itinerary.

### Save Trip

```http
POST /api/trips/save
```

Request:

```json
{
  "userMessage": "Plan a 7-day trip from Austin to Dubai under $1500",
  "origin": "Austin",
  "destination": "Dubai",
  "budget": 1500,
  "days": 7
}
```

Saves the basic trip details only.

### List Saved Trips

```http
GET /api/trips
```

Returns saved trips ordered by newest first.

### Get Saved Trip

```http
GET /api/trips/{id}
```

Returns one saved trip by id. Returns `404 Not Found` when the saved trip does not exist.

### Delete Saved Trip

```http
DELETE /api/trips/{id}
```

Deletes one saved trip by id. Returns `204 No Content` when deleted and `404 Not Found` when the saved trip does not exist.

## Project Structure

```text
ai-travel-agent/
  backend/
    src/main/java/com/aitravelagent/
      config/          CORS configuration
      controller/      Health and trip API controllers
      dto/             Request and response DTOs
      entity/          JPA entities
      repository/      Spring Data repositories
      service/         Mock trip planning and saved trip services
    src/main/resources/
      application.properties
    src/test/
      java/            Spring Boot API tests
      resources/       H2 test database config
  frontend/
    src/
      app/             Redux store and typed hooks
      components/      Reusable UI components
      data/            Demo fallback data
      features/        RTK Query trip API
      pages/           Landing, planner, saved trips, and 404 pages
      services/        Base RTK Query API config
```

## Environment Files

Use `.env.example` as a reference for local database values. Real `.env` files are ignored by Git. Do not commit secrets.
