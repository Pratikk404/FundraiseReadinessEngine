# Fundraise Readiness Engine — Product Roadmap
> From prototype to deployment-ready product

---

## Current Status: Phase 6 Complete ✅

| Component | Status |
|-----------|--------|
| Spring Boot backend (auth, entities, rules engine) | ✅ Done |
| 5 compliance rules (DilutionSum, DPIIT, ESOP, ShareClass, Valuation) | ✅ Done |
| 5 synthetic test companies with seed data | ✅ Done |
| JWT authentication | ✅ Done |
| H2 in-memory database (local dev) | ✅ Done |
| Docker Compose (PostgreSQL, MongoDB, Redis) | ✅ Done |
| Technical spec v2.0 | ✅ Done |
| React dashboard (Landing, Login, Register, Dashboard, Upload, GapReport) | ✅ Done |
| Document upload & CSV/XLSX parser | ✅ Done |
| Node.js processing worker (BullMQ + pdf-parse + MongoDB) | ✅ Done |
| Gap report generation + PDF export | ✅ Done |
| Backend tests (9 test files — rules, auth, integration) | ✅ Done |
| CI/CD (GitHub Actions — backend tests, frontend build, worker check) | ✅ Done |
| Swagger/OpenAPI documentation | ✅ Done |
| Email notifications (async on compliance check) | ✅ Done |
| Admin dashboard (system stats endpoint) | ✅ Done |
| Rate limiting, input sanitization, security headers | ✅ Done |
| Deployment configs (Railway backend + Vercel frontend) | ✅ Done |
| **Actual live deployment** | 🔄 In progress |

---

## What's Built vs. What's Left

### TIER 1 — Core Features

#### 1. React Dashboard ✅ DONE
- [x] Login / Register pages
- [x] Company profile setup (create company)
- [x] Document upload flow (drag & drop, file type validation)
- [x] Readiness score dashboard (overall + per-category breakdown)
- [x] Findings list with severity badges (CRITICAL / WARNING / INFO)
- [x] Before/after score tracking (score history endpoint)
- [x] Responsive design (Tailwind CSS)
- [ ] Profile settings page — *not started*

#### 2. Document Upload & Parsing ✅ DONE
- [x] File upload endpoint (multipart, stored locally)
- [x] CSV/XLSX cap table parser (Apache POI in backend)
- [x] Auto-extraction of equity events from uploaded cap table
- [x] Auto-extraction of share classes
- [x] Document processing status tracking
- [x] Error handling for corrupt/unsupported files

#### 3. Unit Tests ✅ DONE
- [x] DilutionSumRule tests
- [x] DpiitRecognitionRule tests
- [x] EsopConsistencyRule tests
- [x] ShareClassConsistencyRule tests
- [x] ValuationConsistencyRule tests
- [x] RulesEngine integration test
- [x] Auth flow tests (register, login, JWT validation)
- [x] Full flow integration test
- [ ] Frontend tests — *not started*

#### 4. CI/CD ✅ DONE
- [x] GitHub Actions workflow (build + test on push/PR)
- [x] Backend test job (JDK 17, Maven)
- [x] Frontend build job (Node 20, npm ci + build)
- [x] Worker install check job
- [x] Dockerfiles (backend, frontend, worker)
- [x] Deployment configs (Railway + Vercel)
- [ ] Live deployment verification — *in progress*

---

### TIER 2 — Production Features

#### 5. Document Processing Pipeline ✅ DONE
- [x] Node.js worker service (Express + BullMQ)
- [x] PDF text extraction (pdf-parse)
- [x] Regex-based entity extraction for structured fields
- [x] MongoDB storage for raw parsed text
- [x] Queue integration (Redis-backed BullMQ)
- [ ] Dead-letter queue for failed jobs — *not started*

#### 6. Intelligent Gap Report ✅ DONE
- [x] Report generation service (template-based)
- [x] Category-grouped findings (Cap Table, DPIIT, FEMA, ESOP, Share Structure, Valuation)
- [x] Severity-based coloring (RED = must fix, YELLOW = should fix, GREEN = advisory)
- [x] Priority actions section
- [x] PDF export of gap report (HTML-based printable)
- [x] Source document reference for each finding

#### 7. User Management & Onboarding
- [x] JWT authentication (register, login)
- [x] Role-based access (basic — admin vs user)
- [ ] Email verification on registration — *not started*
- [ ] Password reset flow — *not started*
- [ ] Advisor view (can see multiple companies) — *not started*
- [ ] Profile settings page — *not started*

#### 8. Notifications ✅ DONE
- [x] Email notification when compliance check completes
- [ ] Email notification when readiness score changes — *not started*
- [ ] In-app notification bell — *not started*

---

### TIER 3 — Monetization Features

#### 9. Pricing & Payments — NOT STARTED
- [ ] Pricing page (Free / Pro / Enterprise tiers)
- [ ] Stripe integration
- [ ] Subscription management

#### 10. Landing Page & Marketing ✅ DONE
- [x] Landing page (hero, features, CTA)
- [ ] SEO optimization — *not started*
- [ ] Demo video walkthrough — *not started*
- [ ] Blog / content marketing setup — *not started*

#### 11. Analytics & Monitoring — NOT STARTED
- [ ] User analytics (signup funnel, feature usage)
- [ ] Error tracking (Sentry)
- [ ] Performance monitoring
- [ ] Admin dashboard (user count, revenue, active companies)

#### 12. Security & Compliance ✅ MOSTLY DONE
- [x] HTTPS enforcement (via deployment platforms)
- [x] Rate limiting (per user, per IP)
- [x] Input sanitization (XSS prevention)
- [x] CORS lockdown (production origins only)
- [x] Security headers
- [ ] JWT secret rotation — *not started*
- [ ] Data encryption at rest — *not started*
- [ ] GDPR compliance (data export, deletion) — *not started*
- [ ] Privacy policy & terms of service — *not started*

---

### TIER 4 — Scale Features

#### 13. API & Integrations ✅ PARTIALLY DONE
- [x] REST API documentation (OpenAPI/Swagger)
- [ ] API key management for enterprise users — *not started*
- [ ] Webhook support — *not started*
- [ ] Zapier / n8n integration — *not started*

#### 14. Advanced Features — NOT STARTED
- [ ] Multi-language support (Hindi, Marathi, Tamil)
- [ ] WhatsApp notification integration
- [ ] CAPIF/regulatory update alerts
- [ ] Cap table comparison tool (before/after round modeling)
- [ ] Investor readiness checklist

#### 15. Infrastructure ✅ DONE
- [x] PostgreSQL (Docker Compose)
- [x] MongoDB (Docker Compose)
- [x] Redis (Docker Compose)
- [ ] S3 / object storage for documents — *not started*
- [ ] CDN for static assets — *not started*
- [ ] Auto-scaling configuration — *not started*
- [ ] Database backups & disaster recovery — *not started*
- [ ] Staging environment — *not started*

---

## Build Order

| Phase | Status | What Got Built | Milestone |
|-------|--------|----------------|-----------|
| **Phase 0** ✅ | Done | Backend scaffold, 5 rules, seed data, auth | Backend compiles and runs |
| **Phase 1** ✅ | Done | Unit tests (9 files), document upload, CSV/XLSX parser | Can upload cap table → see findings |
| **Phase 2** ✅ | Done | React dashboard (6 pages), score visualization, gap report | Working demo end-to-end |
| **Phase 3** ✅ | Done | Node worker, Docker Compose full stack | Real document processing |
| **Phase 4** ✅ | Done | CI/CD (GitHub Actions), deployment configs | Automated builds |
| **Phase 5** ✅ | Done | Rate limiting, security hardening | Production-ready security |
| **Phase 6** ✅ | Done | Swagger docs, email notifications, admin dashboard | API docs + monitoring |
| **Phase 7** 🔄 | In Progress | Railway backend + Vercel frontend deployment | Live on the internet |
| **Phase 8** | Planned | Email verification, password reset, profile settings | Full user management |
| **Phase 9** | Planned | Pricing page, Stripe integration | Ready to accept paying users |
| **Phase 10** | Planned | Analytics, monitoring, staging env | Production operations |

---

## What's Left for MVP (Sellable Product)

### Must-do before launch:
1. **Verify live deployment** — Railway backend + Vercel frontend are configured but need to be tested
2. **Email verification** — founders need to confirm email before using the tool
3. **Password reset** — can't sell if users get locked out
4. **Profile settings** — basic account management
5. **Pricing page + Stripe** — the actual money-making layer

### Nice-to-have before launch:
6. Frontend tests (current: 0)
7. Dead-letter queue for failed document processing
8. S3 storage for documents (currently local filesystem)
9. JWT secret rotation
10. GDPR data export/deletion

---

## Revenue Model

| Tier | Price | Features |
|------|-------|----------|
| **Free** | ₹0 | 1 company, basic checks, no PDF export |
| **Pro** | ₹999/month | Unlimited companies, full checks, PDF export, email reports |
| **Advisor** | ₹4,999/month | Multiple client companies, bulk analysis, white-label reports |
| **Enterprise** | Custom | API access, custom rules, SSO, dedicated support |

**Target market:** 50,000+ Indian startups registered with DPIIT, plus ~5,000 advisory firms doing pre-fundraise work.

**Competitive advantage:** No other tool verifies actual documents against Indian regulatory patterns. Advisory firms charge ₹50,000-2,00,000 for this manually. This tool does it for ₹999/month.
