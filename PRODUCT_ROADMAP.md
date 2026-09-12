# Fundraise Readiness Engine — Product Roadmap
> From compliance checker to full fundraise lifecycle platform

---

## Current Status: Phase 7 Complete ✅

| Component | Status |
|-----------|--------|
| Spring Boot backend (auth, entities, rules engine) | ✅ Done |
| 5 compliance rules (DilutionSum, DPIIT, ESOP, ShareClass, Valuation) | ✅ Done |
| 5 synthetic test companies with seed data | ✅ Done |
| JWT authentication | ✅ Done |
| Email verification on registration | ✅ Done |
| Password reset flow | ✅ Done |
| Profile settings (update name/email, change password) | ✅ Done |
| H2 in-memory database (local dev) | ✅ Done |
| Docker Compose (PostgreSQL, MongoDB, Redis) | ✅ Done |
| Technical spec v2.0 | ✅ Done |
| React dashboard (Landing, Login, Register, Dashboard, Upload, GapReport) | ✅ Done |
| Email verification page + flow | ✅ Done |
| Forgot/Reset password pages | ✅ Done |
| Settings page (profile, password, plan display) | ✅ Done |
| Dedicated pricing page with Stripe checkout | ✅ Done |
| Document upload & CSV/XLSX parser | ✅ Done |
| Node.js processing worker (BullMQ + pdf-parse + MongoDB) | ✅ Done |
| Gap report generation + PDF export | ✅ Done |
| Backend tests (49 tests — rules, auth, integration) | ✅ Done |
| CI/CD (GitHub Actions — backend tests, frontend build, worker check) | ✅ Done |
| Swagger/OpenAPI documentation | ✅ Done |
| Email notifications (async on compliance check + score changes) | ✅ Done |
| Admin dashboard (system stats endpoint) | ✅ Done |
| Rate limiting, input sanitization, security headers | ✅ Done |
| Stripe integration (checkout sessions, webhooks, plan management) | ✅ Done |
| Deployment configs (Railway backend + Vercel frontend) | ✅ Done |

---

## The Vision: From Checker to Platform

```
Founder plans to raise
        ↓
Stratum runs diagnostic          ← Phase 0-7 (DONE)
        ↓
Produces readiness report        ← Phase 0-7 (DONE)
        ↓
Founder fixes gaps (with guidance) ← Phase 8 (BUILDING NOW)
        ↓
Stratum supports the raise        ← Phase 10-11
        ↓
M&A / transaction advisory        ← Phase 12-13
```

### The Knowledge Moat

| Layer | What | Defensibility |
|-------|------|--------------|
| **Rules** | DilutionSum, DPIIT, ESOP, ShareClass, Valuation | You already have this ✅ |
| **Proprietary failure patterns** | "Startups with X pattern fail 73% of the time" | Only comes from accumulating data |
| **Historical cases** | "Company Y had this issue, fixed it this way" | Network effect — more users = better patterns |
| **Workflow** | Guided fix steps, not just "here's your problem" | Retains users, increases LTV |
| **Advisor expertise** | Encode what top advisors know into the tool | Differentiator vs. self-serve |
| **Network** | Investors, advisors, service providers | Two-sided marketplace potential |

---

## Build Order

| Phase | Status | What Gets Built | Milestone |
|-------|--------|----------------|-----------|
| **Phase 0-7** ✅ | Done | Core platform: rules engine, dashboard, auth, payments | Working product |
| **Phase 8** 🔨 | Building | Fix-it guidance: step-by-step fix instructions per finding | Diagnostic → actionable |
| **Phase 9** | Planned | Historical case library: "50 companies had this issue, here's how they fixed it" | Knowledge moat begins |
| **Phase 10** | Planned | Deploy to Railway + Vercel | Live on the internet |
| **Phase 11** | Planned | Investor matching: readiness score → relevant investors | Full fundraise support |
| **Phase 12** | Planned | Advisor marketplace: certified advisors review reports | Two-sided network |
| **Phase 13** | Planned | M&A / transaction advisory module | Full lifecycle |

---

## Phase 8: Fix-It Guidance (Current Focus)

### What it is
After the diagnostic runs and shows findings, each finding now has:
- **What's wrong** — the finding description (already exists)
- **Why it matters** — the risk/investor concern (new)
- **How to fix it** — step-by-step instructions (new)
- **What good looks like** — example of a corrected state (new)
- **Estimated effort** — quick fix / moderate / needs advisor (new)

### Why it matters
- Transforms the tool from "here's your score" to "here's exactly what to do"
- Founders don't need to hire an advisor just to understand the findings
- Increases engagement — founders come back to check off fixes
- Generates data on what fixes work → feeds the knowledge moat

### Backend changes
- `FindingGuide` entity with fix steps, risk explanation, example, effort level
- `GET /api/compliance/findings/:id/guide` endpoint
- Pre-seeded guide content for all 5 rules

### Frontend changes
- Updated GapReport page with expandable fix steps per finding
- Effort badges (quick fix / moderate / needs advisor)
- "Mark as fixed" flow with before/after tracking

---

## Phase 9: Historical Case Library

### What it is
A growing database of real (anonymized) cases:
- "Company X had dilution math summing to 94% — they issued 6% ESOP top-up, re-ran check, passed"
- "Company Y was missing DPIIT recognition — filed for recognition, took 3 weeks, then angel tax risk cleared"

### Why it matters
- Each case makes the tool smarter — pattern recognition across companies
- Founders see "companies like me had this issue" — reduces anxiety, increases trust
- Builds the proprietary dataset that no competitor has

---

## Phase 10: Deployment

### What it is
Railway (backend) + Vercel (frontend) — the deployment configs are ready, just need to go live.

### Why it matters
- Can't build the knowledge moat without users
- Can't iterate without real feedback
- The product is ready — just needs to be deployed

---

## Phase 11: Investor Matching

### What it is
Based on readiness score, company stage, and sector, match founders with relevant investors:
- "Your readiness score is 85/100 — these 5 investors fund at your stage"
- "You're DPIIT-recognized — these investors require it"
- "Your ESOP pool is clean — these investors care about that"

### Why it matters
- Completes the fundraise lifecycle — not just "are you ready?" but "here's who to talk to"
- Revenue opportunity (investors pay for deal flow)
- Two-sided network effect

---

## Phase 12: Advisor Marketplace

### What it is
Certified advisors can:
- Review readiness reports
- Offer guidance on fix steps
- Be matched with founders who need help
- Build reputation through successful engagements

### Why it matters
- Some findings genuinely need human expertise (legal, tax, FEMA)
- Advisors get deal flow; founders get expert help
- Platform takes a cut — new revenue stream

---

## Phase 13: M&A / Transaction Advisory

### What it is
Extended module for:
- Due diligence document management
- Transaction readiness scoring
- Buyer/seller matching
- Deal process workflow

### Why it matters
- Same document verification infrastructure, different use case
- M&A advisory is ₹50K-2L per deal in India
- Natural extension once you have the document parsing + compliance engine

---

## Revenue Model (Current + Future)

| Tier | Price | Features |
|------|-------|----------|
| **Free** | ₹0 | 1 company, basic checks, no PDF export |
| **Pro** | ₹999/month | Unlimited companies, full checks, PDF export, fix guidance |
| **Advisor** | ₹4,999/month | Multiple client companies, bulk analysis, white-label reports |
| **Enterprise** | Custom | API access, custom rules, SSO, dedicated support |

**Future revenue streams:**
- Investor matching fees
- Advisor marketplace commissions
- M&A transaction advisory

**Target market:** 50,000+ Indian startups registered with DPIIT, plus ~5,000 advisory firms doing pre-fundraise work.
