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

## License

This project is for educational and commercial use.
