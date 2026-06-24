# AI Travel Agent

Full-stack MVP for an AI Travel Agent concept.

The app uses:
- React + TypeScript + Vite frontend
- Spring Boot Java backend
- PostgreSQL for saved trip plans
- Spring Security + JWT authentication
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
$env:JWT_SECRET="replace_with_a_long_random_secret_for_local_dev"
$env:JWT_EXPIRATION_SECONDS="86400"
```

If your PostgreSQL user, password, host, or database name is different, update those values for your machine.

`JWT_SECRET` signs local JWTs. Use a long random value for real local development and never commit real secrets.

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

All endpoints are protected by JWT authentication except:

- `GET /api/health`
- `POST /api/auth/register`
- `POST /api/auth/login`

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

### Authentication

```http
POST /api/auth/register
```

Request:

```json
{
  "fullName": "Faisal Mayar",
  "email": "test@example.com",
  "password": "Password@123"
}
```

Returns:

```json
{
  "token": "jwt-token-here",
  "user": {
    "id": 1,
    "fullName": "Faisal Mayar",
    "email": "test@example.com"
  }
}
```

```http
POST /api/auth/login
```

Request:

```json
{
  "email": "test@example.com",
  "password": "Password@123"
}
```

Returns the same token and user response shape as registration.

```http
GET /api/auth/me
```

Requires:

```http
Authorization: Bearer jwt-token-here
```

Returns the authenticated user profile.

Passwords are hashed with BCrypt before storage. Plain text passwords are not stored.

Frontend test credentials shown in the forms:

```text
Email: test@example.com
Password: Password@123
```

Register the account first, then sign in with those credentials.

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

### Trip Itinerary Items

```http
GET /api/trips/{tripId}/itinerary
```

Returns itinerary items for a saved trip ordered by day number and start time.

```http
POST /api/trips/{tripId}/itinerary
```

Request:

```json
{
  "dayNumber": 1,
  "title": "Arrival and check-in",
  "description": "Arrive, settle in, and take an evening walk.",
  "location": "Hotel",
  "startTime": "15:00",
  "endTime": "18:00"
}
```

Creates an itinerary item.

```http
PUT /api/trips/{tripId}/itinerary/{itemId}
```

Updates an existing itinerary item and returns the updated item.

```http
DELETE /api/trips/{tripId}/itinerary/{itemId}
```

Deletes one itinerary item. Returns `204 No Content` when deleted.

### Trip Tags

```http
GET /api/trips/{tripId}/tags
```

Returns tags for a saved trip.

```http
POST /api/trips/{tripId}/tags
```

Request:

```json
{
  "name": "Beach"
}
```

Creates a tag for the saved trip. Duplicate tag names are ignored case-insensitively for the same trip.

```http
DELETE /api/trips/{tripId}/tags/{tagId}
```

Deletes one tag. Returns `204 No Content` when deleted.

Trip itinerary and tag endpoints return `404 Not Found` when the saved trip does not exist, the child item does not exist, or the child item does not belong to the specified trip.

### Export Trip

```http
GET /api/trips/{id}/export
```

Returns a `text/plain` trip export with basic trip info, notes, checklist items, documents, budget items, itinerary items, and tags. The MVP exports plain text only, not PDF.

### Dashboard Summary

```http
GET /api/dashboard/summary
```

Returns dashboard totals for trips, favorites, notes, checklist items, completed checklist items, documents, budget item amount, itinerary items, tags, and recent trips.

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
      service/         Auth, JWT, mock trip planning, and saved trip services
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
      features/        Auth state and RTK Query APIs
      pages/           Auth, landing, planner, saved trips, dashboard, trip details, preferences, and 404 pages
      services/        Base RTK Query API config
```

## Environment Files

Use `.env.example` as a reference for local database values. Real `.env` files are ignored by Git. Do not commit secrets.
