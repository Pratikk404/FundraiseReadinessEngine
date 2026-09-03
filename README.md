# Fundraise Readiness Engine

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-8-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![Tailwind](https://img.shields.io/badge/Tailwind-4-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)
![Node.js](https://img.shields.io/badge/Node.js-20-339933?style=for-the-badge&logo=node.js&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-7-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-24-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

---

A document-driven compliance diagnostic for early-stage Indian startups. Founders upload actual legal and financial documents (cap table, incorporation cert, board resolutions, financial statements), and the system parses, cross-validates, and scores them against a rules engine that encodes the specific failure patterns investors flag during diligence.

## Why This Exists

Existing fundraise-readiness tools are self-report questionnaires — founders answer questions about themselves and receive a score based on their own word. Cap-table platforms (Carta, Qapita) manage ownership records but don't verify compliance.

This tool does the opposite: it ingests real documents and checks them against the regulatory patterns that kill deals in Indian startup diligence — dilution math that doesn't sum to 100%, missing DPIIT recognition, informal ESOP promises, FEMA red flags, inconsistent share classes.

## What It Checks

| Rule | What It Catches |
|------|----------------|
| **Dilution Sum** | Total equity across all events doesn't sum to 100% |
| **DPIIT Recognition** | Missing or lapsed DPIIT status — Section 56(2)(viib) angel tax exposure |
| **ESOP Consistency** | Informal option grants not reflected in cap table or board-approved pool |
| **Share Class Consistency** | Rights in side letters contradict incorporation documents |
| **Valuation Consistency** | Price per share doesn't align with declared round valuation |

## Architecture

```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐
│  React App  │────▶│ Spring Boot  │────▶│  PostgreSQL  │
│  (Dashboard)│     │  (Main API)  │     │  (Structured)│
└─────────────┘     └──────┬───────┘     └──────────────┘
                           │
                     ┌─────▼──────┐     ┌──────────────┐
                     │   Queue    │────▶│ Node/Express │
                     │ (BullMQ)   │     │  (Worker)    │
                     └────────────┘     └──────┬───────┘
                                               │
                                         ┌─────▼──────┐
                                         │  MongoDB   │
                                         │ (Raw Text) │
                                         └────────────┘
```

## Tech Stack

- **Backend:** Java 17 + Spring Boot 4.0.1 + Spring Security + Spring Data JPA
- **Frontend:** React 18 + Vite 8 + Tailwind CSS 4 + React Query
- **Worker:** Node.js 20 + Express + BullMQ + pdf-parse
- **Database:** PostgreSQL 16 (structured) + MongoDB 7 (unstructured)
- **Queue:** Redis 7 (BullMQ backing)
- **Auth:** JWT (JSON Web Tokens)
- **Build:** Maven (backend), npm (frontend + worker)
- **DevOps:** Docker Compose, GitHub Actions CI
- **Testing:** JUnit 5 (49 tests), Mockito

## Getting Started

### Option 1: Run Locally (H2 — No Docker)

```bash
git clone https://github.com/Pratikk404/FundraiseReadinessEngine.git
cd FundraiseReadinessEngine

# Backend
cd fundraise-backend
./mvnw spring-boot:run

# Frontend (new terminal)
cd ../fundraise-frontend
npm install && npm run dev
```

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`
- Test login: `pratik@test.com` / `test123`

### Option 2: Docker Compose (Full Stack)

```bash
git clone https://github.com/Pratikk404/FundraiseReadinessEngine.git
cd FundraiseReadinessEngine
docker-compose up -d
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Worker Health | http://localhost:3001/health |
| PostgreSQL | localhost:5432 |
| MongoDB | localhost:27017 |
| Redis | localhost:6339 |

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register new user |
| `POST` | `/api/auth/login` | Login, get JWT token |

### Companies
| Method | Endpoint | Auth | Description |
|--------|----------|:----:|-------------|
| `GET` | `/api/companies` | ✅ | List user's companies |
| `GET` | `/api/companies/:id` | ✅ | Get company details |
| `POST` | `/api/companies` | ✅ | Create new company |
| `PUT` | `/api/companies/:id` | ✅ | Update company |

### Documents
| Method | Endpoint | Auth | Description |
|--------|----------|:----:|-------------|
| `POST` | `/api/documents/upload/:companyId` | ✅ | Upload document (multipart) |
| `GET` | `/api/documents/company/:companyId` | ✅ | List company documents |
| `GET` | `/api/documents/:id` | ✅ | Get document details |

### Compliance
| Method | Endpoint | Auth | Description |
|--------|----------|:----:|-------------|
| `POST` | `/api/compliance/check/:companyId` | ✅ | Run full compliance check |
| `GET` | `/api/compliance/findings/:companyId` | ✅ | Get findings (optional `?category=` filter) |
| `GET` | `/api/compliance/scores/:companyId` | ✅ | Get latest readiness scores |
| `GET` | `/api/compliance/scores/:companyId/history` | ✅ | Score history (before/after) |
| `PUT` | `/api/compliance/findings/:id/resolve` | ✅ | Mark finding as resolved |
| `GET` | `/api/compliance/report/:companyId` | ✅ | Gap report (LLM-powered) |
| `GET` | `/api/compliance/report/:companyId/pdf` | ✅ | Printable HTML report |

## Example: Run a Compliance Check

```bash
# 1. Login (test account seeded automatically)
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"pratik@test.com","password":"test123"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 2. List companies
curl http://localhost:8080/api/companies \
  -H "Authorization: Bearer $TOKEN"

# 3. Run compliance check on a company
curl -X POST http://localhost:8080/api/compliance/check/<companyId> \
  -H "Authorization: Bearer $TOKEN"

# 4. Get gap report
curl http://localhost:8080/api/compliance/report/<companyId> \
  -H "Authorization: Bearer $TOKEN"
```

## Test Data

The app auto-seeds 5 synthetic companies on first run:

| Company | Known Issue |
|---------|-------------|
| CleanTech Solutions | No issues (baseline) |
| InnovateHub Technologies | Dilution sums to 94% |
| GreenEnergy India | Missing DPIIT recognition |
| EduLearn Platforms | 3 informal ESOP grants |
| FinServ Solutions | Inconsistent share classes |

**Test login:** `pratik@test.com` / `test123`

## Project Structure

```
FundraiseReadinessEngine/
├── docker-compose.yml              # Full stack orchestration
├── SPEC_v2.md                      # Technical specification
├── PRODUCT_ROADMAP.md              # Build plan
│
├── fundraise-backend/              # Spring Boot API
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/fundraise/engine/
│       │   ├── config/             # Security, CORS
│       │   ├── controller/         # REST endpoints
│       │   ├── dto/                # Request/response DTOs
│       │   ├── entity/             # JPA entities
│       │   ├── repository/         # Spring Data repos
│       │   ├── rules/              # 5 compliance rules
│       │   ├── security/           # JWT filter
│       │   └── service/            # Business logic
│       │       ├── parser/         # CSV/XLSX parser
│       │       ├── GapReportService
│       │       └── PdfExportService
│       └── test/                   # 49 tests (unit + E2E)
│
├── fundraise-frontend/             # React dashboard
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/
│       ├── components/             # Layout, ScoreChart
│       ├── hooks/                  # useAuth
│       ├── lib/                    # API client
│       └── pages/                  # Landing, Login, Register, Dashboard, Upload, GapReport
│
└── fundraise-worker/               # Node.js document processor
    ├── Dockerfile
    └── src/
        ├── worker.js               # BullMQ job processor
        └── health.js               # Health endpoint
```

## Roadmap

- [x] **Phase 0:** Backend scaffold, 5 rules, seed data, auth
- [x] **Phase 1:** Unit tests (49 tests), document upload, CSV/XLSX cap table parser
- [x] **Phase 2:** React dashboard, gap report, PDF export, score history
- [x] **Phase 3:** Node.js worker, Docker Compose full stack
- [x] **Phase 4:** Landing page, marketing, CI/CD
- [ ] **Phase 5:** Deploy to Railway/Vercel
- [ ] **Phase 6:** Rate limiting, security hardening, email notifications

See `PRODUCT_ROADMAP.md` for the full build plan.

---

*Built by [Pratik Kalambe](https://github.com/Pratikk404) — Document-driven compliance diagnostic for Indian startups.*
