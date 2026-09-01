# Fundraise Readiness Engine — Product Roadmap
> From prototype to deployment-ready product

---

## Current Status: Phase 0 Complete ✅

| Component | Status |
|-----------|--------|
| Spring Boot backend (auth, entities, rules engine) | ✅ Done |
| 5 compliance rules (DilutionSum, DPIIT, ESOP, ShareClass, Valuation) | ✅ Done |
| 5 synthetic test companies with seed data | ✅ Done |
| JWT authentication | ✅ Done |
| H2 in-memory database (local dev) | ✅ Done |
| Docker Compose (PostgreSQL, MongoDB, Redis) | ✅ Done |
| Technical spec v2.0 | ✅ Done |
| React dashboard | ❌ Not started |
| Document upload & parsing | ❌ Not started |
| Node.js processing worker | ❌ Not started |
| LLM-powered gap reports | ❌ Not started |
| Tests | ❌ Not started |
| Deployment infrastructure | ❌ Not started |

---

## What's Missing to Be a Real Product

### TIER 1 — Core Features (Must Have to Demo/Sell)

#### 1. React Dashboard
- [ ] Login / Register pages
- [ ] Company profile setup (name, incorporation date, DPIIT status)
- [ ] Document upload flow (drag & drop, file type validation)
- [ ] Readiness score dashboard (overall + per-category breakdown)
- [ ] Findings list with severity badges (CRITICAL / WARNING / INFO)
- [ ] Before/after score tracking
- [ ] Responsive design (mobile-friendly for founders on the go)

#### 2. Document Upload & Parsing
- [ ] File upload endpoint (multipart, stored to local/S3)
- [ ] CSV/XLSX cap table parser (Apache POI — already in pom.xml)
- [ ] Auto-extraction of equity events from uploaded cap table
- [ ] Auto-extraction of share classes
- [ ] Document processing status tracking (PENDING → PROCESSING → COMPLETED)
- [ ] Error handling for corrupt/unsupported files

#### 3. Unit Tests
- [ ] DilutionSumRule tests (sum=100%, sum<100%, sum>100%, no data)
- [ ] DpiitRecognitionRule tests (recognized, not recognized, unknown, lapsed)
- [ ] EsopConsistencyRule tests (formal vs informal, pool overflow)
- [ ] ShareClassConsistencyRule tests (matching vs mismatched)
- [ ] ValuationConsistencyRule tests (consistent, inconsistent, zero price)
- [ ] Auth flow tests (register, login, JWT validation)
- [ ] ComplianceService integration test

#### 4. Basic CI/CD
- [ ] GitHub Actions workflow (build + test on push)
- [ ] Dockerfile for backend
- [ ] Deploy to Railway / Render / Fly.io (free tier)

---

### TIER 2 — Production Features (Needed to Actually Sell)

#### 5. Document Processing Pipeline
- [ ] Node.js worker service (Express + BullMQ)
- [ ] PDF text extraction (pdf-parse for text-native, Apache Tika for scanned)
- [ ] Regex-based entity extraction for structured fields
- [ ] MongoDB storage for raw parsed text
- [ ] Queue integration (Redis-backed BullMQ)
- [ ] Dead-letter queue for failed jobs

#### 6. Intelligent Gap Report
- [ ] Report generation service (template-based, deterministic)
- [ ] Category-grouped findings (Cap Table, DPIIT, FEMA, ESOP, Share Structure, Valuation)
- [ ] Severity-based coloring (RED = must fix, YELLOW = should fix, GREEN = advisory)
- [ ] Priority actions section (top 3 things to fix first)
- [ ] PDF export of gap report
- [ ] Source document reference for each finding

#### 7. User Management & Onboarding
- [ ] Email verification on registration
- [ ] Password reset flow
- [ ] Role-based access (Founder, Advisor, Admin)
- [ ] Advisor view (can see multiple companies)
- [ ] Profile settings page

#### 8. Notifications
- [ ] Email notification when processing completes
- [ ] Email notification when readiness score changes
- [ ] In-app notification bell

---

### TIER 3 — Monetization Features (Needed for Revenue)

#### 9. Pricing & Payments
- [ ] Pricing page (Free / Pro / Enterprise tiers)
- [ ] Free tier: 1 company, basic checks, no PDF export
- [ ] Pro tier: Unlimited companies, full checks, PDF export, email reports
- [ ] Enterprise: API access, custom rules, white-label
- [ ] Stripe integration for payments
- [ ] Subscription management

#### 10. Landing Page & Marketing
- [ ] Landing page (hero, features, pricing, testimonials)
- [ ] SEO optimization
- [ ] Demo video walkthrough
- [ ] Blog / content marketing setup

#### 11. Analytics & Monitoring
- [ ] User analytics (signup funnel, feature usage)
- [ ] Error tracking (Sentry)
- [ ] Performance monitoring (response times, DB queries)
- [ ] Admin dashboard (user count, revenue, active companies)

#### 12. Security & Compliance
- [ ] HTTPS enforcement
- [ ] Rate limiting (per user, per IP)
- [ ] Input sanitization (XSS prevention)
- [ ] CORS lockdown (production origins only)
- [ ] JWT secret rotation
- [ ] Data encryption at rest
- [ ] GDPR compliance (data export, deletion)
- [ ] Privacy policy & terms of service

---

### TIER 4 — Scale Features (Needed for Growth)

#### 13. API & Integrations
- [ ] REST API documentation (OpenAPI/Swagger)
- [ ] API key management for enterprise users
- [ ] Webhook support (for advisors who want to integrate)
- [ ] Zapier / n8n integration

#### 14. Advanced Features
- [ ] Multi-language support (Hindi, Marathi, Tamil)
- [ ] WhatsApp notification integration (Indian founders live on WhatsApp)
- [ ] CAPIF/regulatory update alerts (when FEMA/DPIIT rules change)
- [ ] Cap table comparison tool (before/after round modeling)
- [ ] Investor readiness checklist (beyond compliance)

#### 15. Infrastructure
- [ ] PostgreSQL (production, via Docker or managed)
- [ ] MongoDB (production, via Docker or managed)
- [ ] Redis (production, for queue + caching)
- [ ] S3 / object storage for documents
- [ ] CDN for static assets
- [ ] Auto-scaling configuration
- [ ] Database backups & disaster recovery
- [ ] Staging environment

---

## Realistic Build Order

| Phase | Duration | What Gets Built | Milestone |
|-------|----------|----------------|-----------|
| **Phase 0** ✅ | Done | Backend scaffold, rules engine, seed data | Backend compiles and runs |
| **Phase 1** | 1-2 weeks | Unit tests, document upload, CSV parser | Can upload cap table → see findings |
| **Phase 2** | 1-2 weeks | React dashboard, score visualization | Working demo end-to-end |
| **Phase 3** | 1 week | Node worker, PDF parsing, MongoDB | Real document processing |
| **Phase 4** | 1 week | Gap report generation, PDF export | Shareable compliance report |
| **Phase 5** | 1 week | Landing page, pricing, Stripe | Ready to accept paying users |
| **Phase 6** | 1 week | CI/CD, deployment, monitoring | Live on the internet |

**Total: ~6-7 weeks to a sellable product**

---

## Day-by-Day Plan for This Week

| Day | Task |
|-----|------|
| **Day 1 (Tomorrow)** | Install Docker, spin up PostgreSQL + MongoDB, fix compliance check 403 bug, run end-to-end test |
| **Day 2** | Write unit tests for all 5 rules, fix any edge cases found |
| **Day 3** | Build document upload endpoint + CSV/XLSX cap table parser |
| **Day 4** | Scaffold React project, build login/register pages |
| **Day 5** | Build company profile setup + document upload UI |
| **Day 6** | Build readiness dashboard with score visualization |
| **Day 7** | Wire everything end-to-end: upload → parse → check → score → display |

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
