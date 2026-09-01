# Fundraise Readiness Engine

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

## Tech Stack

- **Backend:** Java 17 + Spring Boot 4.0.1 + Spring Security + Spring Data JPA
- **Database:** PostgreSQL (production) / H2 (local dev)
- **Auth:** JWT (JSON Web Tokens)
- **Build:** Maven
- **DevOps:** Docker Compose

## Getting Started

### Prerequisites
- Java 17+
- Docker & Docker Compose

### Run Locally

```bash
# Clone the repo
git clone https://github.com/Pratikk404/FundraiseReadinessEngine.git
cd FundraiseReadinessEngine/fundraise-backend

# Run with H2 (no Docker needed)
./mvnw spring-boot:run
```

The server starts at `http://localhost:8080`.

### With Docker (PostgreSQL + MongoDB + Redis)

```bash
# From the project root
docker-compose up -d

# Then update application.properties to use PostgreSQL profile
# and restart the backend
```

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

### Compliance
| Method | Endpoint | Auth | Description |
|--------|----------|:----:|-------------|
| `POST` | `/api/compliance/check/:companyId` | ✅ | Run full compliance check |
| `GET` | `/api/compliance/findings/:companyId` | ✅ | Get findings (optional `?category=` filter) |
| `GET` | `/api/compliance/scores/:companyId` | ✅ | Get readiness scores |
| `PUT` | `/api/compliance/findings/:id/resolve` | ✅ | Mark finding as resolved |

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
├── docker-compose.yml          # PostgreSQL + MongoDB + Redis
├── SPEC_v2.md                  # Full technical specification
├── PRODUCT_ROADMAP.md          # Build plan from prototype to product
└── fundraise-backend/
    ├── pom.xml                 # Maven dependencies
    ├── mvnw                    # Maven wrapper
    └── src/main/java/com/fundraise/engine/
        ├── config/             # Security, CORS, app config
        ├── controller/         # REST endpoints (Auth, Company, Compliance, Health)
        ├── dto/                # Request/response DTOs
        ├── entity/             # JPA entities (Company, User, Finding, etc.)
        ├── repository/         # Spring Data JPA repositories
        ├── rules/              # Compliance rule engine
        │   ├── ComplianceRule.java      # Rule interface
        │   ├── ComplianceContext.java   # Data passed to rules
        │   ├── RulesEngine.java         # Orchestrates all rules
        │   ├── DilutionSumRule.java     # Equity sum check
        │   ├── DpiitRecognitionRule.java # DPIIT status check
        │   ├── EsopConsistencyRule.java  # ESOP grant validation
        │   ├── ShareClassConsistencyRule.java
        │   └── ValuationConsistencyRule.java
        ├── security/           # JWT filter + utilities
        └── service/            # Business logic (Auth, Company, Compliance)
```

## Roadmap

- [x] **Phase 0:** Backend scaffold, 5 rules, seed data, auth
- [ ] **Phase 1:** Unit tests, document upload, CSV/XLSX parser
- [ ] **Phase 2:** React dashboard, score visualization
- [ ] **Phase 3:** Node.js worker, PDF parsing, MongoDB
- [ ] **Phase 4:** Gap report generation, PDF export
- [ ] **Phase 5:** Landing page, pricing, Stripe
- [ ] **Phase 6:** CI/CD, deployment, monitoring

See `PRODUCT_ROADMAP.md` for the full build plan.

---

*Progress updated daily until the final product is ready.*
