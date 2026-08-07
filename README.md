# Meals on Wheels Plus

Meals on Wheels Plus is a course-project MVP coordinating three separate services for seniors:

1. Meal Delivery
2. Companion Visit
3. RoboCompanion Visit

A RoboCompanion represents a physical assistive robot resource in the system. The MVP manages robot inventory, assignment, scheduling, and visit status only; it does not integrate with real robot hardware.

## Features and roles

- **Senior:** sign in, manage a care profile, request all three services, track status and schedules, see assigned robot details, cancel eligible requests, and view notifications.
- **Volunteer:** view and complete only assigned Meal Delivery and human Companion Visit work. A volunteer is not treated as a RoboCompanion.
- **Admin:** manage users, requests, and dashboards; manage RoboCompanion inventory and maintenance/activation; schedule visits; assign only available robots; and complete robot visits.
- Backend-enforced JWT authentication, role and ownership checks, validated state transitions, BCrypt passwords, validation errors, and in-app notifications.

## Technology and architecture

- React 18, TypeScript, Vite
- Java 17, Spring Boot 3.3, Spring Security, Spring Data JPA
- PostgreSQL 16 (H2 is test-only)
- Docker Compose locally; multi-stage Docker and `heroku.yml` deployment

The application is a modular monolith. The browser sends JSON and JWTs to Spring REST controllers, services enforce lifecycle and ownership rules, and Spring Data JPA persists to PostgreSQL. The production Spring Boot process also serves the compiled React application.

## Data model and lifecycle

- `users` has optional one-to-one senior or volunteer profiles.
- `meal_requests` and `companion_requests` belong to seniors and may reference volunteers.
- `robo_companions` stores physical robot inventory, a unique asset tag, activation, and operational status.
- `robocompanion_visit_requests` belongs to a senior and optionally references one RoboCompanion. A robot retains many historical visits when deactivated.
- `notifications` belongs to a user.

Status flows are constrained:

- Meal: `REQUESTED → APPROVED → ASSIGNED → PREPARING → OUT_FOR_DELIVERY → DELIVERED`
- Companion: `REQUESTED → APPROVED → SCHEDULED → ASSIGNED → IN_PROGRESS → COMPLETED`
- RoboCompanion Visit: `REQUESTED → APPROVED → SCHEDULED → ASSIGNED → IN_PROGRESS → COMPLETED`
- Robot lifecycle: `AVAILABLE → ASSIGNED → IN_SERVICE → AVAILABLE`

Admins can reject pending requests. Seniors can cancel before service begins. Cancellation or rejection releases an assigned robot. Maintenance, inactive, assigned, and in-service robots cannot be newly assigned. A locked inventory lookup prevents concurrent double assignment.

## Quick start

Prerequisite: Docker Desktop with Compose.

```bash
docker compose up --build
```

Open <http://localhost:8080>. PostgreSQL starts, the schema is updated, and demo data is seeded idempotently. Use `docker compose down` to stop without deleting data. `docker compose down -v` resets local data and is destructive.

For development, start PostgreSQL with `docker compose up -d postgres`, run `mvn spring-boot:run` in `backend`, then `npm ci` and `npm run dev` in `frontend`. Vite serves <http://localhost:5173> and proxies `/api` to port 8080.

## Demo data

| Role | Email | Local password |
|---|---|---|
| Admin | `admin@mealsplus.local` | `Admin123!` |
| Senior | `senior@mealsplus.local` | `Senior123!` |
| Volunteer | `volunteer@mealsplus.local` | `Volunteer123!` |

Robots: `RC-01 / Stretch Alpha / AVAILABLE`, `RC-02 / Stretch Beta / AVAILABLE`, and `RC-03 / Stretch Gamma / MAINTENANCE`. Set `DEMO_SEED_ENABLED=false` in production or override demo credentials with secure configuration.

## Configuration

Key variables are `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DATABASE_URL` (Heroku), `JWT_SECRET`, `JWT_EXPIRATION_MS`, `PORT`, `CORS_ALLOWED_ORIGINS`, `DEMO_SEED_ENABLED`, and the `DEMO_*` account values. See `.env.example` for local defaults. Schema evolution uses the existing `spring.jpa.hibernate.ddl-auto=update` MVP strategy.

## API overview

All endpoints except login and health require `Authorization: Bearer <token>`.

| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/login`, `GET /api/auth/me` |
| Profile | `GET/PUT /api/profile`, role profile updates |
| Users (admin) | `GET/POST /api/users`, `PUT/DELETE /api/users/{id}` |
| Meals | `/api/meal-requests` create/my/assigned/admin update/volunteer status/cancel operations |
| Companion Visits | Equivalent operations under `/api/companion-requests` |
| RoboCompanion inventory (admin) | `GET/POST /api/robocompanions`, `GET/PUT /api/robocompanions/{id}`, `GET /api/robocompanions/available` |
| RoboCompanion Visits | Senior `POST`, `GET /my`, `GET /{id}`, `DELETE /{id}`; admin `GET`, `GET /{id}`, `PUT /{id}` under `/api/robocompanion-requests` |
| Notifications | `GET /api/notifications`, `PUT /api/notifications/{id}/read` |
| Dashboard | `GET /api/dashboard` (admin), including robot/request metrics |
| Health | `GET /api/health` |

See `api-requests.http` for executable examples.

## Tests and builds

```bash
cd backend
mvn clean test
mvn package

cd ../frontend
npm ci
npm run build
```

Backend tests cover authentication, role and ownership authorization, all existing workflows, RoboCompanion creation and notifications, invalid robot assignment, cancellation release, and request/robot lifecycle transitions. See `MANUAL_TEST_CHECKLIST.md` for manual end-to-end workflows.

## Deployment

The existing multi-stage Docker image compiles the frontend into Spring Boot static resources and honors `PORT`. `docker-compose.yml`, `Dockerfile`, `heroku.yml`, and `app.json` remain compatible. Heroku deployments should provision PostgreSQL and set a strong `JWT_SECRET`; `DATABASE_URL` is parsed automatically.

## Repository layout

```text
backend/                 Spring Boot API, security, persistence, tests
frontend/                React/Vite application
Dockerfile               Production multi-stage build
docker-compose.yml       Local app + PostgreSQL
heroku.yml / app.json    Heroku container metadata
api-requests.http        API smoke examples
MANUAL_TEST_CHECKLIST.md End-to-end verification steps
```

## Known limitations and future work

- There is no robot control, ROS, telemetry, navigation, video, chatbot, voice assistant, or hardware API integration.
- The MVP treats any assigned/in-progress visit as an exclusive robot booking; visit duration and partial time-window overlap are not modeled.
- Notifications refresh with the page; there is no WebSocket, SMS, or email delivery.
- Production successors should add Flyway migrations, audit logs, rate limiting, calendar optimization, stronger password policy, frontend test automation, and database backups.
