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

Filter favorites:

```http
GET /api/trips?favorite=true
```

### Get Saved Trip

```http
GET /api/trips/{id}
```

Returns one saved trip by id. Returns `404 Not Found` when the saved trip does not exist.

### Search Saved Trips

```http
GET /api/trips/search?q=Dubai
```

Searches saved trips by destination, origin, or original user message.

### Update Saved Trip

```http
PUT /api/trips/{id}
```

Request:

```json
{
  "userMessage": "Plan a 5-day trip from Austin to Tokyo under $2200",
  "origin": "Austin",
  "destination": "Tokyo",
  "budget": 2200,
  "days": 5
}
```

Updates the saved trip fields and returns the updated trip. Returns `404 Not Found` when the saved trip does not exist.

### Favorite Saved Trip

```http
PATCH /api/trips/{id}/favorite
```

Toggles the saved trip favorite status and returns the updated trip.

### Duplicate Saved Trip

```http
POST /api/trips/{id}/duplicate
```

Creates a copy of an existing saved trip with a new id and timestamps.

### Delete Saved Trip

```http
DELETE /api/trips/{id}
```

Deletes one saved trip by id. Returns `204 No Content` when deleted and `404 Not Found` when the saved trip does not exist.

### Trip Notes

```http
GET /api/trips/{tripId}/notes
```

Returns notes for a saved trip ordered by newest first.

```http
POST /api/trips/{tripId}/notes
```

Request:

```json
{
  "content": "Ask for a hotel near the metro."
}
```

Creates a note for the saved trip.

```http
PUT /api/trips/{tripId}/notes/{noteId}
```

Request:

```json
{
  "content": "Updated note content."
}
```

Updates an existing note and returns the updated note.

```http
DELETE /api/trips/{tripId}/notes/{noteId}
```

Deletes a note from the saved trip. Returns `204 No Content` when deleted.

### Trip Checklist

```http
GET /api/trips/{tripId}/checklist
```

Returns checklist items for a saved trip ordered by oldest first.

```http
POST /api/trips/{tripId}/checklist
```

Request:

```json
{
  "title": "Pack passport"
}
```

Creates a checklist item with `completed` set to `false`.

```http
PATCH /api/trips/{tripId}/checklist/{itemId}
```

Toggles the checklist item completed status and returns the updated item.

```http
DELETE /api/trips/{tripId}/checklist/{itemId}
```

Deletes a checklist item. Returns `204 No Content` when deleted.

Trip note and checklist endpoints return `404 Not Found` when the saved trip does not exist, the child item does not exist, or the child item does not belong to the specified trip.

### Trip Documents

```http
GET /api/trips/{tripId}/documents
```

Returns document metadata for a saved trip ordered by newest first.

```http
POST /api/trips/{tripId}/documents
```

Request:

```json
{
  "name": "Passport scan",
  "type": "Passport",
  "url": "https://example.com/passport"
}
```

Creates a text-only document metadata record. The MVP does not upload or store real files.

```http
DELETE /api/trips/{tripId}/documents/{documentId}
```

Deletes one document metadata record. Returns `204 No Content` when deleted.

### Trip Budget Items

```http
GET /api/trips/{tripId}/budget-items
```

Returns budget items for a saved trip ordered by oldest first.

```http
POST /api/trips/{tripId}/budget-items
```

Request:

```json
{
  "title": "Flight",
  "category": "Transport",
  "amount": 850.00
}
```

Creates a budget item.

```http
PUT /api/trips/{tripId}/budget-items/{itemId}
```

Request:

```json
{
  "title": "Airport taxi",
  "category": "Ground transport",
  "amount": 65.25
}
```

Updates an existing budget item and returns the updated item.

```http
DELETE /api/trips/{tripId}/budget-items/{itemId}
```

Deletes one budget item. Returns `204 No Content` when deleted.

Trip document and budget item endpoints return `404 Not Found` when the saved trip does not exist, the child item does not exist, or the child item does not belong to the specified trip.

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
      pages/           Landing, planner, saved trips, trip details, and 404 pages
      services/        Base RTK Query API config
```

## Environment Files

Use `.env.example` as a reference for local database values. Real `.env` files are ignored by Git. Do not commit secrets.
