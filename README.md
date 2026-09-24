# Campus Passport

Campus Passport is a verified-campus social network for discovering communities, collaborating on student projects, chatting in scoped spaces, and joining campus games.

## First Slice

This repository starts with the platform foundation, campus-email verification flow, and passport summary screen.

- `backend`: Spring Boot modular monolith API
- `frontend`: Angular standalone client
- `docs/architecture.md`: source-of-truth architecture and delivery boundaries

## Run

```powershell
cd frontend
npm install
npm start
```

The Angular client expects the API at `http://localhost:8080/api` and falls back to a local demo response when the API is unavailable.

The backend can be run with Maven or the Maven wrapper once Maven is installed:

```powershell
cd backend
mvn spring-boot:run
```
