# Fundraise Readiness Engine — Technical Specification v2.0
> A document-driven compliance & cap-table diagnostic for early-stage Indian startups
> Reviewed & Updated: September 1, 2026

---

## 1. Executive Summary

Existing fundraise-readiness tools (Startup Steroid's Readiness Score, DeckFix AI, Deloitte's SelfAssess) are self-report questionnaires — founders answer questions about themselves and receive a score based on their own word. Cap-table platforms (Carta, Qapita) solve the adjacent but different problem of ownership record-keeping. Neither ingests and verifies a founder's actual legal and financial documents against the specific failure patterns that trip up early-stage Indian startups.

**YC's Fall 2026 Requests for Startups** names this exact category ("AI-Native Compliance Infrastructure") as an open gap: financial compliance is still stitched together with spreadsheets, siloed tools, and expensive headcount, and the pain is worst where regulatory patchwork varies by jurisdiction — which is precisely the DPIIT / FEMA / ESOP situation for Indian startups.

This project builds a **document-driven diagnostic**: founders upload real artifacts (cap table export, incorporation certificate, board resolutions, financial statements), and the system parses, cross-validates, and scores them against a rules engine and ML layer that encodes exactly the pattern-recognition a deal-advisory firm sells as a service — productized into a self-serve tool.

---

## 2. Full Stack Coverage

| Skill | Role in this system | Why it's load-bearing, not decorative |
|-------|---------------------|--------------------------------------|
| **Java / Spring Boot** | Primary API — auth, rules engine, document orchestration, scoring | System of record. All compliance logic lives here as testable, versioned business logic. |
| **Node.js / Express (MERN)** | Async document-processing microservice — OCR/text extraction queue, webhook/notification worker | Decoupled from main API so long-running parsing jobs never block user requests. Talks to Spring Boot over REST + message queue. |
| **React (MERN)** | Founder-facing dashboard | Upload flow, readiness score breakdown, before/after tracking as issues are fixed. |
| **PostgreSQL (Database #1)** | Structured, relational data: cap table events, checklist state, scoring history, audit trail | Dilution math and share-class consistency checks need transactional integrity and joins across time — a relational model is the correct fit. |
| **MongoDB (Database #2)** | Unstructured data: raw parsed document text, extracted-entity JSON, document metadata | Board resolutions and SHAs vary wildly in structure; schema-flexible storage avoids forcing irregular legal text into rigid columns. |
| **ML / AI** | NLP entity extraction, cap-table anomaly detection, LLM-generated gap report | The genuinely hard engineering: turning unstructured legal/financial documents into structured, checkable facts, and turning rule-engine output into a readable report. |
| **Docker / docker-compose** | Local dev environment, reproducible builds | Two databases + two services = docker-compose is non-negotiable for onboarding and demo. |

---

## 3. Problem Statement

### 3.1 Who this is for
Early-stage Indian founders (pre-seed to Series A) preparing for a fundraise or acquisition, and the advisors/analysts who support them.

### 3.2 The specific failure patterns being encoded

| # | Failure Pattern | Why it matters | Rule |
|---|----------------|----------------|------|
| 1 | **Dilution math errors** | Cumulative equity issuance across funding rounds does not sum to 100%, often from spreadsheet drift or forgotten ESOP top-ups | `DilutionSumRule` |
| 2 | **Missing or lapsed DPIIT recognition** | Directly affects angel tax exposure under Section 56(2)(viib) — a common last-minute diligence blocker | `DpiitRecognitionRule` |
| 3 | **Informal ESOP promises** | Verbal or emailed option grants never reflected in the actual cap table or board-approved pool | `EsopConsistencyRule` |
| 4 | **FEMA red flags** | Foreign investment received without correct reporting (FC-GPR filings, sectoral cap checks, pricing guideline compliance) | `FemaPricingRule` |
| 5 | **Inconsistent share classes** | Rights promised in side letters that contradict the official share class structure in incorporation documents | `ShareClassConsistencyRule` |
| 6 | **Valuation mismatch** | Price per share in cap table doesn't align with declared valuation — a diligence red flag that kills deals | `ValuationConsistencyRule` |

---

## 4. System Architecture

### 4.1 High-level flow

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

### 4.2 Flow description
1. Founder uploads documents (PDF/XLSX/DOCX) via the React dashboard.
2. Spring Boot API receives the upload, persists metadata to PostgreSQL, and pushes a processing job to a queue.
3. Node/Express worker consumes the queue: runs OCR/text extraction (Apache Tika / pdf-parse), stores raw extracted text and structured entities in MongoDB.
4. Spring Boot's rules engine reads the extracted entities from MongoDB, runs DPIIT/FEMA/ESOP/dilution checks, and writes structured findings to PostgreSQL.
5. An LLM call (via the ML/AI layer) turns raw rule-engine findings into a readable, categorized gap report.
6. React dashboard polls/fetches the readiness score and gap report, rendered by category with before/after tracking as issues are resolved.

### 4.3 Why two backend services
Document parsing (OCR, large PDF extraction, LLM calls) is slow and bursty — it should never block the request/response cycle of the main API. Isolating it in a Node/Express worker connected via a queue (BullMQ backed by Redis) keeps the Spring Boot API responsive and makes the parsing service independently scalable. This is a realistic, defensible architecture decision to discuss in an interview, rather than an artificial excuse to use two languages.

---

## 5. Backend Deep Dive — Spring Boot (Primary API)

### 5.1 Module breakdown

| Module | Responsibility |
|--------|---------------|
| `auth` | Spring Security + JWT; founder accounts, role-based access (founder vs. advisor view) |
| `documents` | Upload handling, metadata persistence, presigned-URL storage (S3-compatible), job dispatch to queue |
| `rules-engine` | DPIIT / FEMA / ESOP / dilution / share-class / valuation checks as independently testable rule classes |
| `cap-table` | Domain model for equity events, rounds, share classes; dilution math over time |
| `scoring` | Aggregates rule-engine findings into a weighted readiness score by category |
| `reports` | Orchestrates the LLM call to turn findings into a narrative gap report; caches results |
| `audit` | Audit trail for every finding: who uploaded, when, what was checked, what changed between runs |

### 5.2 Rules engine design

Each compliance check is implemented as a small, independently unit-testable class implementing a common `ComplianceRule` interface:

```java
public interface ComplianceRule {
    String getRuleId();           // e.g., "DILUTION_SUM_100"
    String getCategory();         // e.g., "CAP_TABLE"
    Severity getSeverity();       // CRITICAL, WARNING, INFO
    List<Finding> evaluate(ComplianceContext context);
}
```

This keeps the DPIIT/FEMA/ESOP logic auditable and easy to extend — a new rule is a new class, not a change to a monolithic scoring function. This is the part of the system that most directly encodes domain knowledge and is the strongest interview talking point.

### 5.3 Data integrity
Cap table and dilution data is transactional (PostgreSQL, via Spring Data JPA/Hibernate). Equity events are stored as an **append-only ledger** (round, instrument type, shares issued, price, date) so dilution can be recomputed at any point in time rather than trusting a single stored percentage — this mirrors how a real cap table should be modeled and is a stronger design than a flat "current ownership %" table.

---

## 6. Backend Deep Dive — Node.js / Express (Processing Service)

- Consumes jobs from the queue: `{documentId, storageUrl, documentType}`
- Runs text extraction: Apache Tika (via a lightweight wrapper) for DOCX/XLSX/scanned PDFs, pdf-parse for text-native PDFs
- Runs a first-pass NLP entity extraction (see Section 8) and writes structured JSON + raw text to MongoDB
- Emits a completion event back to Spring Boot (webhook or queue message) so the rules engine can proceed
- Also owns notification delivery (email/webhook) when a founder's readiness score changes after re-upload
- **Rate limiting and retry:** LLM calls use exponential backoff; PDF parsing failures go to a dead-letter queue; corrupted/password-protected files are flagged with a clear error status

---

## 7. Frontend — React

| Screen | Purpose |
|--------|---------|
| **Company Profile** | Company details, DPIIT status, incorporation date, founder info. Compliance health overview between uploads. |
| **Upload flow** | Drag-and-drop for cap table export, incorporation docs, board resolutions, financials; shows per-document processing status |
| **Readiness dashboard** | Overall score + breakdown by category (Cap Table, DPIIT, FEMA, ESOP, Share Structure, Valuation) |
| **Gap report** | Narrative findings per category, severity-tagged, with the specific document/clause referenced |
| **Before / after tracking** | Score history across re-uploads as founders fix flagged issues |

State management: React Query for server state / polling job status; Tailwind CSS for styling.

---

## 8. ML / AI Layer

### 8.1 Entity extraction
Unstructured legal/financial text (board resolutions, SHAs, cap table exports) is passed through a two-stage extraction:
1. **Rule-based / regex pre-extraction** for highly structured fields (dates, share counts, percentages, entity names)
2. **LLM prompt-based extraction** for fields needing contextual understanding (e.g., "was this ESOP grant approved by the board or only promised informally?")

### 8.2 Anomaly detection
Cap table dilution math is checked **programmatically (not ML)** for correctness — sum of ownership percentages, share-class consistency over time. This is deliberately rule-based rather than ML-based, because the correctness bar for financial math should be deterministic, not probabilistic. ML is reserved for tasks that genuinely require language understanding: extraction and report generation.

### 8.3 Gap report generation
Structured findings from the rules engine are passed to an LLM with a fixed prompt template:

```
You are a compliance advisor for Indian startups. Based on the following rule-engine findings,
generate a founder-readable gap report.

## Findings
{findings_json}

## Requirements
- Group by category (Cap Table, DPIIT, FEMA, ESOP, Share Structure, Valuation)
- Severity: RED = must fix before fundraise, YELLOW = should fix, GREEN = advisory
- Reference the specific document/clause behind each finding
- End with a "Priority Actions" section (top 3 things to fix first)
```

---

## 9. Data Model

### PostgreSQL (Structured)

```sql
-- Companies
CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    incorporation_date DATE,
    dpiit_status VARCHAR(50) DEFAULT 'UNKNOWN',  -- RECOGNIZED, NOT_RECOGNIZED, UNKNOWN
    dpiit_recognition_date DATE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Founders
CREATE TABLE founders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    company_id UUID REFERENCES companies(id),
    role VARCHAR(50) DEFAULT 'FOUNDER',
    created_at TIMESTAMP DEFAULT NOW()
);

-- Documents
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID REFERENCES companies(id),
    type VARCHAR(50) NOT NULL,  -- CAP_TABLE, INCORPORATION, BOARD_RESOLUTION, FINANCIAL_STATEMENT
    filename VARCHAR(255),
    storage_url VARCHAR(500),
    upload_date TIMESTAMP DEFAULT NOW(),
    processing_status VARCHAR(50) DEFAULT 'PENDING'  -- PENDING, PROCESSING, COMPLETED, FAILED
);

-- Equity Events (append-only ledger)
CREATE TABLE equity_events (
    id BIGSERIAL PRIMARY KEY,
    company_id UUID REFERENCES companies(id),
    round_name VARCHAR(100),
    instrument_type VARCHAR(50) NOT NULL,  -- COMMON, PREFERRED, ESOP_POOL, CONVERSION
    shares_issued BIGINT NOT NULL,
    price_per_share DECIMAL(15, 4),
    event_date DATE NOT NULL,
    source_document_id UUID REFERENCES documents(id),
    created_at TIMESTAMP DEFAULT NOW()
);

-- ESOP Grants
CREATE TABLE esop_grants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID REFERENCES companies(id),
    grantee VARCHAR(255),
    shares BIGINT NOT NULL,
    board_approved BOOLEAN DEFAULT FALSE,
    grant_date DATE,
    source_document_id UUID REFERENCES documents(id),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Share Classes
CREATE TABLE share_classes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID REFERENCES companies(id),
    class_name VARCHAR(100) NOT NULL,  -- ORDINARY, PREFERRED_A, PREFERRED_B, etc.
    rights_description TEXT,
    total_shares BIGINT,
    source_document_id UUID REFERENCES documents(id),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Findings (rule engine output)
CREATE TABLE findings (
    id BIGSERIAL PRIMARY KEY,
    company_id UUID REFERENCES companies(id),
    rule_id VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,  -- CRITICAL, WARNING, INFO
    description TEXT NOT NULL,
    source_document_id UUID REFERENCES documents(id),
    resolved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Readiness Scores
CREATE TABLE readiness_scores (
    id BIGSERIAL PRIMARY KEY,
    company_id UUID REFERENCES companies(id),
    category VARCHAR(50) NOT NULL,
    score DECIMAL(5, 2),  -- 0.00 to 100.00
    computed_at TIMESTAMP DEFAULT NOW()
);

-- Audit Log
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    company_id UUID REFERENCES companies(id),
    action VARCHAR(50) NOT NULL,  -- UPLOAD, REPROCESS, SCORE_CHANGE, FINDING_RESOLVED
    actor_id UUID,
    details JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_equity_events_company ON equity_events(company_id, event_date);
CREATE INDEX idx_findings_company ON findings(company_id, category, severity);
CREATE INDEX idx_readiness_scores_company ON readiness_scores(company_id, computed_at);
CREATE INDEX idx_documents_company ON documents(company_id, type);
CREATE INDEX idx_audit_log_company ON audit_log(company_id, action, created_at);
```

### MongoDB (Unstructured)

```javascript
// Collection: parsed_documents
{
    _id: ObjectId,
    documentId: UUID,           // references PostgreSQL documents.id
    companyId: UUID,
    rawText: "...",             // full extracted text
    ocrMetadata: {
        engine: "tika|pdf-parse",
        confidence: 0.95,
        processingTime: "2.3s"
    },
    createdAt: ISODate
}

// Collection: extracted_entities
{
    _id: ObjectId,
    documentId: UUID,
    companyId: UUID,
    documentType: "CAP_TABLE|BOARD_RESOLUTION|SHA|FINANCIAL_STATEMENT",
    entities: {
        // Type-specific extracted fields
        // For CAP_TABLE: { shareClasses: [...], totalShares: ..., equityEvents: [...] }
        // For BOARD_RESOLUTION: { date: ..., resolution: ..., approvedBy: [...] }
        // For SHA: { clauses: [...], shareClasses: [...], rights: [...] }
    },
    extractionMethod: "regex|llm|hybrid",
    confidence: 0.92,
    createdAt: ISODate
}
```

---

## 10. MVP Scope vs. Phase 2

### Phase 0 — Environment Setup (2-3 days)
- Docker Compose: PostgreSQL, MongoDB, Redis
- Spring Boot project scaffold with Spring Security + JWT auth
- React project scaffold with Tailwind + React Query
- CI pipeline skeleton (GitHub Actions)

### Phase 1 — MVP (1.5-2 weeks)
- Cap table upload + CSV/XLSX parsing
- Dilution math engine (`DilutionSumRule`)
- DPIIT recognition status check (`DpiitRecognitionRule`)
- Basic readiness score (2 categories) + React dashboard
- Seed data: 5 synthetic companies with known issues
- Unit tests for rule classes

### Phase 2 — Full Feature (2-3 weeks)
- FEMA compliance checks (FC-GPR filing presence, sectoral cap validation)
- ESOP grant cross-referencing against board resolutions (`EsopConsistencyRule`)
- Share class consistency check (`ShareClassConsistencyRule`)
- Valuation consistency check (`ValuationConsistencyRule`)
- Full document parsing pipeline (Node/Express worker, MongoDB, NLP extraction)
- LLM-generated narrative gap report
- Before/after score tracking across re-uploads
- Audit trail

---

## 11. Week-by-Week Build Plan

| Week | Focus |
|------|-------|
| **0** | Environment setup (Docker, PostgreSQL, MongoDB, Redis), Spring Boot + React scaffolds, auth skeleton |
| **1** | Auth, company/founder models, PostgreSQL schema, cap table upload endpoint (CSV/XLSX parsing) |
| **2** | Dilution math engine, DPIIT check, scoring aggregation, React upload + dashboard v1 |
| **3** | MVP demo-ready: seed data (5 synthetic companies), rule engine unit tests, dashboard polish |
| **4** | Node/Express worker scaffolded, queue integration, MongoDB for raw text |
| **5** | NLP entity extraction, FEMA rule checks, ESOP cross-referencing |
| **6** | LLM gap report, before/after tracking, end-to-end demo video, README |

---

## 12. Synthetic Data Strategy

Five test companies, each designed to trigger specific failure patterns:

| Company | Name | Issues |
|---------|------|--------|
| A | **CleanTech Solutions Pvt Ltd** | No issues — baseline/control company |
| B | **InnovateHub Technologies** | Dilution math sums to 94% (forgot ESOP top-up after Series A) |
| C | **GreenEnergy India Pvt Ltd** | Missing DPIIT recognition — Section 56(2)(viib) angel tax exposure |
| D | **EduLearn Platforms** | Informal ESOP promises (3 verbal grants) not reflected in cap table |
| E | **FinServ Solutions** | Inconsistent share classes — side letter promises different rights than incorporation docs |

---

## 13. Resume & Interview Framing

### 13.1 Resume bullets
1. Built a document-driven fundraise-readiness diagnostic for Indian startups, encoding DPIIT/FEMA/ESOP compliance patterns from real deal-advisory experience into a testable rules engine (Java/Spring Boot, PostgreSQL).
2. Designed a two-service architecture (Spring Boot + Node/Express) separating transactional compliance logic from async document/NLP processing, backed by PostgreSQL (relational) and MongoDB (document store).
3. Implemented an NLP + LLM pipeline to extract structured entities from unstructured legal documents (board resolutions, SHAs) and generate founder-readable compliance gap reports.

### 13.2 Interview story arc
Lead with the domain gap (self-report tools vs. document-verified diagnostics), not the tech stack. Explain the architecture decision (why two backend services) as a reasoned trade-off, not a skills checklist. Be ready to walk through one specific rule (e.g., dilution sum-to-100% check) end-to-end: input document, extraction, rule logic, output finding — this is the moment that proves depth over breadth.

### 13.3 Questions they'll ask

| They'll ask | Your answer |
|-------------|-------------|
| "Why two databases?" | PostgreSQL for transactional integrity on equity events; MongoDB for variable-structure legal document text |
| "Why two backend services?" | Compliance logic must be synchronous and fast; document parsing is async and bursty |
| "How do you handle LLM hallucination?" | Deterministic rules for math; LLM only for extraction and narrative; every finding references source document |
| "What about regulatory changes?" | Rules are pluggable classes — adding a new check is one class, not a refactor |
| "Why not just use Carta/Qapita?" | They manage cap tables; they don't verify compliance against Indian regulatory patterns |

---

## 14. Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| No access to real client documents | Build against synthetic/anonymized sample cap tables and template legal documents — never use real Stratum client data |
| Regulatory rules are nuanced and change over time | Scope MVP to a small, well-verified rule set (dilution math + DPIIT); cite sources for each rule in code comments |
| LLM extraction accuracy on messy documents | Combine rule-based pre-extraction for structured fields with LLM only for context-dependent fields; always show the source document/clause behind a finding so it's human-verifiable |
| Scope creep (trying to build all of Phase 2) | Timebox the MVP strictly; treat Phase 2 items as explicitly deferred roadmap, not requirements |
| XLSX parsing complexity (cap tables from different tools have wildly different formats) | Start with a fixed CSV/XLSX template that founders download and fill; add flexible parsing later |

---

## 15. Tech Stack Summary

```
Backend:     Java 17 + Spring Boot 4.0.1 + Spring Security + Spring Data JPA
Worker:      Node.js 20 + Express + BullMQ + pdf-parse + Apache Tika wrapper
Frontend:    React 18 + Vite 8 + Tailwind CSS 4 + React Query
Database 1:  PostgreSQL 16
Database 2:  MongoDB 7
Queue:       Redis 7 (BullMQ backing)
ML/AI:       OpenAI / Anthropic API (via HTTP client)
Payments:    Stripe (checkout sessions, webhooks, subscriptions)
DevOps:      Docker Compose, GitHub Actions, Railway, Vercel
Build:       Maven (backend), npm (frontend + worker)
```

---

## 16. Platform Vision: From Checker to Lifecycle

### 16.1 The evolution

The system starts as a point-in-time compliance checker and evolves into a full fundraise lifecycle platform:

```
Founder plans to raise
        ↓
Stratum runs diagnostic          ← Phases 0-7 (DONE)
        ↓
Produces readiness report        ← Phases 0-7 (DONE)
        ↓
Founder fixes gaps (with guidance) ← Phase 8
        ↓
Stratum supports the raise        ← Phases 10-11
        ↓
M&A / transaction advisory        ← Phases 12-13
```

### 16.2 The knowledge moat

The long-term defensibility comes from accumulating proprietary knowledge that no competitor has:

| Layer | Description | Defensibility |
|-------|-------------|---------------|
| **Rules** | Deterministic compliance checks (DilutionSum, DPIIT, ESOP, ShareClass, Valuation) | Replicable but requires domain expertise |
| **Proprietary failure patterns** | "Startups with X pattern fail 73% of the time in diligence" | Only comes from accumulating data across companies |
| **Historical cases** | "Company Y had this issue, fixed it this way, raised successfully" | Network effect — more users = better patterns |
| **Workflow** | Guided fix steps, not just "here's your problem" | Retains users, increases LTV |
| **Advisor expertise** | Encode what top advisors know into the tool | Differentiator vs. self-serve tools |
| **Network** | Investors, advisors, service providers on platform | Two-sided marketplace potential |

### 16.3 Why this works in India

- 50,000+ DPIIT-registered startups need fundraise readiness
- Advisory firms charge ₹50,000-2,00,000 for manual compliance checks
- No tool verifies actual documents against Indian regulatory patterns
- The DPIIT/FEMA/ESOP regulatory landscape is uniquely Indian — global tools don't apply
- Founders want to self-serve but don't know what they don't know

---

## 17. Fix-It Guidance System

### 17.1 Problem

The current system tells founders "your dilution math is wrong" but doesn't tell them how to fix it. Founders still need to hire an advisor to understand and resolve findings.

### 17.2 Solution

Each finding now comes with structured fix guidance:

```json
{
  "findingId": "uuid",
  "ruleId": "DILUTION_SUM_100",
  "description": "Total equity sums to 94%, not 100%",
  "guide": {
    "whyItMatters": "Investors will flag this immediately. Dilution math must sum to exactly 100% or the cap table is considered unreliable.",
    "howToFix": [
      "Review all equity events since incorporation",
      "Check if any ESOP top-ups were promised but not recorded",
      "Verify the 6% gap — likely an unrecorded ESOP pool allocation",
      "Update the cap table to include the missing allocation",
      "Re-run the compliance check to verify the fix"
    ],
    "whatGoodLooksLike": "All equity events recorded: Founders 70% + Seed 20% + ESOP Pool 10% = 100% exactly.",
    "effortLevel": "QUICK_FIX",
    "estimatedTime": "30 minutes",
    "needsAdvisor": false,
    "relatedCases": [
      "InnovateHub Technologies had the same issue — missing 6% ESOP allocation",
      "FinServ Solutions had a similar gap from unrecorded convertible notes"
    ]
  }
}
```

### 17.3 Effort levels

| Level | Description | Founder can self-fix? |
|-------|-------------|---------------------|
| `QUICK_FIX` | Minor data entry or document update | Yes |
| `MODERATE` | Requires understanding of legal/regulatory context | Maybe, with guidance |
| `NEEDS_ADVISOR` | Involves legal filings, FEMA compliance, or board resolutions | No — needs professional help |

### 17.4 Pre-seeded guides

Each rule gets a guide template:

| Rule | Effort | Typical fix |
|------|--------|-------------|
| DilutionSumRule | QUICK_FIX | Record missing equity event |
| DpiitRecognitionRule | NEEDS_ADVISOR | File for DPIIT recognition (3-4 weeks) |
| EsopConsistencyRule | MODERATE | Formalize informal grants via board resolution |
| ShareClassConsistencyRule | MODERATE | Update incorporation docs or side letters |
| ValuationConsistencyRule | QUICK_FIX | Correct price per share in cap table |

### 17.5 Data model

```sql
CREATE TABLE finding_guides (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_id VARCHAR(100) NOT NULL UNIQUE,
    why_it_matters TEXT NOT NULL,
    how_to_fix JSONB NOT NULL,           -- Array of step strings
    what_good_looks_like TEXT NOT NULL,
    effort_level VARCHAR(20) NOT NULL,    -- QUICK_FIX, MODERATE, NEEDS_ADVISOR
    estimated_time VARCHAR(50),
    needs_advisor BOOLEAN DEFAULT FALSE,
    related_cases JSONB,                  -- Array of case reference strings
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

---

## 18. Future Phases

### Phase 9: Historical Case Library
- Anonymized case database: "Company X had this issue, fixed it this way"
- Pattern recognition across companies
- Founders see "companies like me" — reduces anxiety, increases trust

### Phase 10: Deployment
- Railway (backend) + Vercel (frontend)
- Configs already in place

### Phase 11: Investor Matching
- Readiness score → relevant investors
- "Your score is 85 — these 5 investors fund at your stage"
- Revenue opportunity (investors pay for deal flow)

### Phase 12: Advisor Marketplace
- Certified advisors review reports
- Matched with founders who need help
- Platform takes a commission

### Phase 13: M&A / Transaction Advisory
- Due diligence document management
- Transaction readiness scoring
- Buyer/seller matching
- Same infrastructure, different use case
