# Campus Passport Architecture

## Product Boundary

Campus Passport is a privacy-conscious, verified-campus social network. Campus-only data is visible only after campus-email verification. Metrics that cannot be obtained from an integration are user-imported, device-assisted, or future work; the product must never fabricate them.

## Runtime Shape

The first release is a modular monolith:

```text
Angular PWA
    |
    v
Spring Boot REST API
    |
    +-- auth       registration, email verification, sessions
    +-- passport   profile, privacy, connections
    +-- community  feeds, groups, board membership
    +-- project    student project publishing and collaboration
    +-- messaging  scoped direct messages and chat
    +-- games      campus games and leaderboards
    +-- moderation reports, moderation actions, audit events
    +-- shared     security, web errors, events, observability
    |
    v
PostgreSQL 16 + Flyway
```

Each module owns its API, application use cases, domain objects, and infrastructure adapters. Cross-module behavior uses application ports or domain events rather than direct table access.

## Package Layout

Backend packages follow `com.campuspassport.<module>/{api,application,domain,infrastructure}` with shared concerns under `com.campuspassport.shared/{security,web,events,observability}`.

Frontend uses standalone Angular components and feature folders: `core` for API/session concerns, `shared` for reusable presentation, and `features/<module>` for routes and screens.

## First Slice API

| Method | Route | Purpose | Access |
| --- | --- | --- | --- |
| `GET` | `/api/health` | Service readiness probe | Public |
| `POST` | `/api/auth/register` | Create a pending account and verification challenge | Public |
| `POST` | `/api/auth/verify-email` | Consume a one-time campus-email challenge | Public |
| `GET` | `/api/passport/me` | Read the current passport summary | Verified student |

The initial implementation uses an in-memory repository so the route contract can be exercised before persistence is introduced. The production contract is designed for hashed one-time tokens, 15-minute expiry, attempt limits, resend cooldowns, and campus-only authorization.

## Core Data Model

Initial entities are `users`, `verification_tokens`, `passport_profiles`, `communities`, `community_memberships`, `projects`, `project_collaborators`, `conversations`, `messages`, `games`, `game_scores`, `reports`, and `audit_events`. The first migration creates the identity and passport foundations; later slices add the remaining aggregates.

## Security and Privacy

- Campus-email verification is mandatory before campus-only content is visible.
- Student MFA is optional; moderator and admin MFA is required.
- Passwords and verification tokens are stored as hashes, never plaintext.
- Moderator/admin actions emit audit events and use a separate admin surface.
- Third-party integrations must document permissions and unsupported metrics.

## Delivery Order

1. Platform foundation, auth verification, passport summary.
2. Communities, campus directory, feeds, and privacy controls.
3. Projects, collaboration lifecycle, ratings, and scoped messaging.
4. Games, opt-in leaderboards, placements, and permitted integrations.
5. Moderation, audit surface, PWA offline cache, hardening, and production observability.
