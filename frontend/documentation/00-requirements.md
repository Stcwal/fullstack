# IDATT2105 — Project Requirements Reference

**Source**: IDATT2105 Fullstack project 2026.pdf (official assignment document)  
**Status**: Authoritative. Do not deviate from these requirements without explicit justification.  
**Rule**: This document MUST be consulted before any implementation or design decision.

---

## 1. General Requirements

| # | Requirement | Status |
|---|-------------|--------|
| 1.1 | Submission deadline: **Friday 10th April 2026, kl. 14:00** (Inspera) | ⏳ |
| 1.2 | One submission per group. Names of all participants included. | ⏳ |
| 1.3 | Unfinished functionality will **not** be evaluated — do NOT include half-finished features | ⚠️ Critical |
| 1.4 | Every part: programmed solution, documentation, and presentation must be **very good quality** for grade A or B | ⚠️ Critical |

---

## 2. Technical Requirements

### 2.1 Frontend

| Requirement | Detail | Status |
|-------------|--------|--------|
| Framework | **Vue.js v3** (related libraries allowed) | ✅ Done |
| CSS | **Clean CSS only. No Tailwind. No Bootstrap.** | ✅ Done |
| Auth storage | **sessionStorage** for short-lived login session | ✅ Done |
| Universal design | OWASP + WCAG accessibility principles | ✅ Partial |
| Universal design detail | Semantic HTML, aria-labels, keyboard navigation, colour contrast | ⚠️ Needs audit |

### 2.2 Backend (backend engineer's responsibility — frontend must align)

| Requirement | Detail |
|-------------|--------|
| Language | Java v17, v21 or v25 |
| Framework | Spring Boot v3 / Spring Framework v5 or v6 |
| Database | **MySQL v8 or v9** and/or **H2** |
| Authentication | JWT + Spring Security |
| DB access | Spring JDBC or Spring JPA |
| Code coverage | **≥ 50%** (tests required) |
| CI/CD | Required — must be set up |
| API docs | Swagger — endpoints documented with description and attributes |

### 2.3 CI/CD

| Requirement | Detail | Status |
|-------------|--------|--------|
| CI/CD pipeline | Must be set up and running | ⚠️ Check status |
| Runnable setup | Docker / Maven command must work | ⚠️ Check status |
| DB schema | Flyway or normal DB schema scripts with test data | Backend task |

---

## 3. Documentation Requirements

All documentation must be of high quality. Required:

| # | Requirement | Where | Status |
|---|-------------|-------|--------|
| 3.1 | **Swagger / API docs** — each endpoint explained with attributes | Backend (auto) | Backend task |
| 3.2 | **System documentation** — enables a new dev to quickly get up and running | docs/ | ⚠️ In progress |
| 3.3 | **Architecture sketches / class diagram** | docs/ | ⚠️ Needed |
| 3.4 | **How-to-run instructions** — preferably README file | README | ⚠️ Needed |
| 3.5 | **Test data** — credentials and sample data documented | docs/ | ⚠️ Partial |
| 3.6 | **Prerequisites** — documented if project depends on other modules | docs/ | ⚠️ Needed |
| 3.7 | Other documentation as PDF | docs/ | ⚠️ Needed |

---

## 4. Submission Materials

| # | Requirement |
|---|-------------|
| 4.1 | Zip file including all modules/files |
| 4.2 | Runnable source code (properly documented source + test files, pom.xml, package.json, Dockerfile) |
| 4.3 | Flyway or DB schema scripts with test data |
| **4.4** | **Description of how to run tests and get the system running — as a script, docker/maven command. Must be easy.** |

---

## 5. Product Requirements

### 5.1 Core Architecture

| Requirement | Detail | Status |
|-------------|--------|--------|
| Two top-level modules | **IK-Mat** (food compliance) + **IK-Alkohol** (alcohol compliance) | ⚠️ IK-Alkohol missing |
| Multi-tenant | Multiple organisations use the same platform independently | ⚠️ Frontend stub only |
| Shared infrastructure | Auth, user management, notifications, document storage, reporting | ✅ Partial |

### 5.2 Required Features (from spec — all must be present or justified as excluded)

| Feature | Description | Frontend Status |
|---------|-------------|-----------------|
| **Digital Checklists** | Daily, weekly, monthly task lists for hygiene, cleaning, safety | ✅ Done |
| **Temperature Logging** | Record and monitor food storage temps, alerts for deviations | ✅ Done |
| **Deviation Management** | Report, track, resolve incidents and non-compliance issues | ✅ Done |
| **Alcohol Compliance Tracking** | Routines for responsible alcohol serving and age verification | ❌ Not built |
| **User Roles and Access Control** | Different permission levels for staff, managers, admins | ⚠️ Route guards done, feature-gating missing |
| **Audit and Inspection Reports** | Auto-generated reports for internal reviews and inspections | ⚠️ Stub only |
| **Document Export (PDF/JSON)** | Export reports, logs, documentation | ⚠️ Stub only |
| **Notifications and Reminders** | Alerts for overdue tasks, missing logs, critical issues | ⚠️ Infrastructure only |
| **Document Storage** | Centralised storage of policies, training materials, certifications | ✅ View only (no upload) |
| **Mobile Accessibility** | Responsive design or mobile app for smartphones and tablets | ✅ Tablet done |
| **Data Analytics Dashboard** | Overview of compliance status, trends, performance metrics | ✅ Done |

---

## 6. Assessment Criteria (inferred from assignment)

The grade depends on **all three** of these:

1. **Programmed solution** — does it work, is the code quality good, does it cover the feature list?
2. **Documentation** — Swagger, system docs, architecture, README, test data
3. **Presentation** — video presentation, clarity of demo

> "Every group should assess what they prioritise and complete the chosen functionalities as much as possible. **Unfinished functionality will not be evaluated and should not be part of the delivery.**"

**Implication**: A focused, polished set of fully working features scores higher than many half-finished features. If a feature is included, it must be complete.

---

## 7. OWASP / Security Requirements (frontend-relevant)

| Requirement | Implementation |
|-------------|----------------|
| No XSS | Sanitise all user-generated content rendered in the DOM |
| No SQL injection | N/A on frontend — backend handles via JPA |
| sessionStorage for auth | ✅ Already implemented |
| Input validation | All form inputs must be validated before submission |
| CORS | Backend must whitelist frontend domain (backend task) |
| Auth on all protected routes | ✅ Router guards implemented |

---

## 8. Universal Design / WCAG Requirements (frontend)

| Requirement | Detail | Status |
|-------------|--------|--------|
| Semantic HTML | Use `<button>`, `<nav>`, `<main>`, `<section>`, `<article>` correctly | ⚠️ Needs audit |
| `aria-label` | All interactive elements without visible text labels need aria-labels | ⚠️ Partial |
| Keyboard navigation | All actions reachable by keyboard (Tab, Enter, Escape) | ⚠️ Partial |
| Colour contrast | WCAG AA: 4.5:1 for normal text, 3:1 for large text | ⚠️ Needs audit |
| Focus indicators | Visible focus ring on all focusable elements | ⚠️ Partial |
| No content by colour alone | Status must be communicated with text/icon, not just colour | ⚠️ Partial |

---

## 9. Test Data Requirements

The following test credentials must be documented and working:

| Email | Password | Role |
|-------|----------|------|
| kari@everestsushi.no | admin123 | ADMIN |
| ola@everestsushi.no | leder123 | MANAGER |
| per@everestsushi.no | ansatt123 | STAFF |

Minimum data seeded:
- 2+ organisations
- 3+ users per org with different roles
- 5+ storage units with 7-day temperature history
- Completed checklists + a few open deviations

---

## 10. What Must Be Communicated to Backend

These are **frontend requirements that depend on backend implementation**:

| # | Requirement | Backend action needed |
|---|-------------|----------------------|
| B1 | `GET /api/users/me` — returns full user with firstName, lastName | Implement endpoint |
| B2 | Login response must include `firstName`, `lastName` | Update `LoginResponse` DTO |
| B3 | User management API (`GET/POST/PUT /api/users`) | Implement endpoints |
| B4 | All feature endpoints (units, readings, checklists, deviations, dashboard, documents, reports) | Implement — prioritise in order listed |
| B5 | Age verification log endpoint for IK-Alkohol | New endpoint needed |
| B6 | Alcohol incident log endpoint | New endpoint needed |
| B7 | `GET /api/auth/capabilities` or include permissions in `/users/me` | Decide and implement |

---

*Last updated: 2026-03-31 — Cross-reference with CLAUDE.md for full frontend implementation context.*
