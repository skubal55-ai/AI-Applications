# Salon Management App

A full-stack salon management application built with **Java**, designed to support both **Android** and **iOS** (via shared backend API). Features subscription-based payments with **automatic currency detection** based on user location.

## Architecture

```
salon-backend/    → Spring Boot REST API (Java 17)
salon-android/    → Native Android App (Java)
```

### Backend (Spring Boot)
- **JWT Authentication** — Secure login/register with access & refresh tokens
- **Salon Services Management** — CRUD operations for salon services (haircut, makeup, spa, etc.)
- **Appointment Booking** — Book, view, cancel appointments with staff scheduling
- **Subscription System** — BASIC ($9.99), PREMIUM ($19.99), VIP ($29.99) monthly plans
- **Auto Currency Detection** — Detects user's country via IP geolocation and converts all prices to local currency (supports 40+ countries/currencies)
- **Staff Management** — View available staff, specializations, ratings

### Android App
- **Login/Register** with automatic country & currency detection from device locale
- **Browse Services** with prices in local currency
- **Book Appointments** with date/time picker and staff selection
- **Subscription Management** — View plans, subscribe, cancel
- **Appointment History** — View past and upcoming appointments
- Material Design UI with pull-to-refresh

---

## Supported Currencies (Auto-Selected by Location)

| Region | Countries |
|--------|-----------|
| Asia | India (INR ₹), Japan (JPY ¥), China (CNY ¥), S. Korea (KRW ₩), Singapore (SGD), Malaysia (MYR), Thailand (THB), Indonesia (IDR), Philippines (PHP), Vietnam (VND), Pakistan (PKR), Bangladesh (BDT), Sri Lanka (LKR), UAE (AED), Saudi Arabia (SAR) |
| Europe | UK (GBP £), Germany/France/Italy/Spain/Netherlands (EUR €), Switzerland (CHF), Sweden/Norway/Denmark (SEK/NOK/DKK), Poland (PLN), Russia (RUB), Turkey (TRY) |
| Americas | USA (USD $), Canada (CAD), Mexico (MXN), Brazil (BRL), Argentina (ARS), Colombia (COP), Chile (CLP) |
| Oceania | Australia (AUD), New Zealand (NZD) |
| Africa | South Africa (ZAR), Nigeria (NGN), Kenya (KES), Egypt (EGP) |

---

## Getting Started

### Prerequisites
- **Java 17** or later
- **Maven 3.6+**
- **Android Studio** (for the Android app)

### Backend Setup

```bash
cd salon-backend

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

#### Default Users (Auto-created on first run)

| Email | Password | Role |
|-------|----------|------|
| admin@salon.com | admin123 | ADMIN |
| emma@salon.com | staff123 | STAFF |
| priya@salon.com | staff123 | STAFF |
| customer@test.com | customer123 | CUSTOMER |

### API Endpoints

#### Authentication
- `POST /api/auth/register` — Register new user
- `POST /api/auth/login` — Login
- `POST /api/auth/refresh` — Refresh JWT token

#### Services
- `GET /api/services?countryCode=IN` — Get all services (prices in INR)
- `GET /api/services/category/{category}?countryCode=JP` — Filter by category
- `POST /api/services` — Create service (ADMIN only)

#### Appointments
- `POST /api/appointments` — Book appointment
- `GET /api/appointments/my` — My appointments
- `GET /api/appointments/upcoming` — Upcoming appointments
- `POST /api/appointments/{id}/cancel` — Cancel appointment

#### Subscriptions
- `GET /api/subscriptions/plans?countryCode=IN` — Get plans in INR
- `POST /api/subscriptions/subscribe` — Subscribe to a plan
- `GET /api/subscriptions/active` — Get active subscription
- `POST /api/subscriptions/cancel` — Cancel subscription

#### Currency
- `GET /api/currency/detect` — Auto-detect currency from IP
- `GET /api/currency/{countryCode}` — Get currency info
- `GET /api/currency/convert?amount=10&countryCode=IN` — Convert USD to local
- `GET /api/currency/supported` — List all supported currencies

### Android Setup

1. Open `salon-android/` in Android Studio
2. Update `BASE_URL` in `app/build.gradle` to point to your backend server
3. Build and run on emulator or device

For emulator: The default `http://10.0.2.2:8080/api/` points to localhost.

### iOS Support

The backend API is fully platform-agnostic. An iOS app can be built using:
- **Swift/SwiftUI** consuming the same REST API
- **Flutter** or **React Native** for cross-platform mobile
- The same API endpoints, authentication flow, and currency detection work identically for iOS clients

---

## Subscription Plans

| Plan | USD Price | Features |
|------|-----------|----------|
| BASIC | $9.99/mo | Basic booking, View services, Email reminders |
| PREMIUM | $19.99/mo | Priority booking, 10% discount, Loyalty points, SMS reminders |
| VIP | $29.99/mo | All Premium + Home service, Free cancellation, 20% discount, Priority support |

Prices auto-convert to user's local currency. Example: In India, BASIC = ₹834.67/mo, PREMIUM = ₹1,669.17/mo, VIP = ₹2,504.17/mo.

---

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Backend | Spring Boot 3.2, Spring Security, Spring Data JPA |
| Auth | JWT (jjwt 0.12.5) |
| Database | H2 (dev), MySQL-ready (prod) |
| Build | Maven |
| Android | Java, Retrofit2, Material Components, Glide |
| Geolocation | ip-api.com (free tier) |

---

## GenAI CI/CD Integration

This project ships a complete **Generative AI–powered CI/CD pipeline** built on
[GitHub Actions](https://docs.github.com/en/actions).  Every pipeline component
is in `.github/` and requires only a single secret (`OPENAI_API_KEY`) to unlock
the AI-driven stages.

### Pipeline Overview

```
Pull Request opened / updated
        │
        ├─► Backend CI          – Maven build + JUnit tests
        ├─► Android CI          – Gradle lint + debug APK + unit tests
        ├─► GenAI PR Review     – GPT-4o code review posted as PR comment
        ├─► GenAI Test Suggest  – GPT-4o generates missing test stubs
        └─► GenAI Security Scan – GPT-4o OWASP-aligned vulnerability scan

git tag v*.*.*
        └─► GenAI Release Notes – GPT-4o drafts categorised release notes

Weekly schedule (Mon 02:00 UTC)
        └─► GenAI Security Scan (full codebase)
```

### Workflows

| File | Trigger | Purpose |
|------|---------|---------|
| `.github/workflows/backend-ci.yml` | push / PR to `salon-backend/**` | Maven build, unit tests, JAR artifact |
| `.github/workflows/android-ci.yml` | push / PR to `salon-android/**` | Gradle lint, unit tests, debug APK artifact |
| `.github/workflows/genai-pr-review.yml` | every PR (open/sync) | AI code review comment on the PR |
| `.github/workflows/genai-test-suggestions.yml` | PR touching `src/main/**` | AI-generated JUnit / Android test stubs |
| `.github/workflows/genai-security-scan.yml` | push, PR, weekly cron | OWASP-category security findings; fails build on CRITICAL |
| `.github/workflows/genai-release-notes.yml` | git tag `v*.*.*` or manual | AI-authored, categorised release notes + GitHub Draft Release |

### AI Scripts

All GenAI logic lives in `.github/scripts/` (Python 3.11, dependencies in
`.github/scripts/requirements.txt`):

| Script | What the AI does |
|--------|-----------------|
| `genai_pr_review.py` | Fetches the PR diff → GPT-4o → structured review (CRITICAL / WARNING / SUGGESTION / INFO) posted as PR comment |
| `genai_test_suggestions.py` | Reads changed production Java files → GPT-4o → ready-to-paste JUnit 5 + Mockito / Android JUnit test stubs |
| `genai_security_scan.py` | Reads source files → GPT-4o → OWASP-aligned findings with PoC + remediation snippets; exports `critical_count` step output |
| `genai_release_notes.py` | Collects `git log` since last tag → GPT-4o → human-readable categorised release notes saved to `release-notes.md` |

### Setup

#### 1 — Add the OpenAI secret

In your GitHub repository go to **Settings → Secrets and variables → Actions**
and add:

| Secret name | Value |
|-------------|-------|
| `OPENAI_API_KEY` | Your OpenAI API key (`sk-…`) |

> Without this secret the GenAI jobs exit gracefully (skipped, not failed) so
> the standard build / test jobs always succeed independently.

#### 2 — (Optional) Enable the review gate

Set a repository variable `ENABLE_GENAI_REVIEW=true` to always run the PR
review job even when the secret might not be present (the script will skip
gracefully if the key is missing).

#### 3 — Local development

Run any script locally for testing:

```bash
cd .github/scripts
pip install -r requirements.txt

# PR review (replace SHAs with real values)
OPENAI_API_KEY=sk-… \
GITHUB_TOKEN=ghp_… \
REPO_NAME=owner/repo \
PR_NUMBER=42 \
BASE_SHA=abc123 \
HEAD_SHA=def456 \
python genai_pr_review.py

# Full security scan of the whole repo
OPENAI_API_KEY=sk-… SCAN_MODE=full python genai_security_scan.py
```

### How GenAI Enhances Each Stage

#### Code Review (PR stage)
Traditional CI catches *compilation errors* and *test failures*.  The GenAI
review layer catches **logic errors, security anti-patterns, and design
concerns** that static analysis misses — surfaced automatically before a human
reviewer even looks at the PR.

#### Test Suggestions (PR stage)
The model reads the actual production code that changed and emits concrete
test stubs with real assertions, not just empty `@Test` skeletons.  Engineers
copy, adapt, and commit them — turning coverage gaps into tracked tasks inside
the same PR cycle.

#### Security Scanning (every push + weekly)
Each changed file is audited against the OWASP Top 10 in the context of a
Spring Boot + Android application.  Every finding includes a one-sentence
attack scenario and a remediation code snippet, making it actionable.  The
pipeline fails on `CRITICAL` findings so vulnerabilities cannot be merged
silently.

#### Release Notes (tag push)
Commit messages are rarely user-friendly.  GPT-4o filters noise (merge
commits, typo fixes) and groups related changes into polished "What's New /
Bug Fixes / Security" sections — automatically attached to the GitHub Draft
Release.

### Security Considerations

- The OpenAI key is stored as a GitHub Actions secret and is never logged.
- Diff/file content sent to OpenAI is limited in size
  (`MAX_DIFF_CHARS = 24 000`, `MAX_FILE_CHARS = 8 000`) to stay within
  context limits and avoid inadvertently sending large blobs.
- All three GenAI scripts handle missing secrets gracefully (exit 0, write a
  "skipped" report) so they never block unrelated CI stages.
- The security scan job holds `security-events: write` permission only; no
  `contents: write` is granted to PR-triggered jobs.

---

## License

This project is for educational and commercial use.
