# HydroBill — Smart Water & Consumer Billing (React Frontend)

React frontend for the Infosys Springboard project *"Smart Water and Consumer Billing"*.
Three roles, one approval workflow: **Super Admin → Building Owner → Resident**.

## Stack

- React 18 + Vite
- React Router v6 (role-protected routes)
- Axios (wired for a Spring Boot backend)
- Tailwind CSS (custom "water utility" design system)
- Framer Motion (page/element animations)
- Recharts (owner reports)
- Razorpay Checkout (UPI / cards / wallets)

## Getting started

```bash
npm install
cp .env.example .env
npm run dev
```

Open the printed local URL. The app runs fully **without a backend** out of the box — see "Mock mode" below.

## Demo credentials (mock mode)

This project includes a mock authentication backend for local testing. Credentials are defined in `src/api/mockDb.js` and should not be used in production.

Try the full loop: register a new building at `/register` → log in as Super Admin and approve it in **Pending Requests** → the owner can now log in and add a resident in **Residents** → log a reading in **Meter Entry** → generate a bill in **Bill Generation** → log in as that resident and pay it in **Current Bill**.

## Mock mode (no backend required)

`src/api/mockDb.js` + `src/api/services.js` implement a tiny local "database" in `localStorage`, seeded with sample data. Every page calls functions from `services.js`, never `axios` directly — so switching to the real backend never touches a page component.

To reset the mock data at any time, open devtools console and run:
```js
localStorage.removeItem('awb_db_v1'); location.reload();
```

## Connecting the real Spring Boot backend

1. Set `VITE_USE_MOCK=false` in `.env`.
2. Set `VITE_API_BASE_URL` (or rely on the Vite dev proxy in `vite.config.js`, which forwards `/api/*` to `http://localhost:8080`).
3. Implement the endpoints below — each `services.js` function has the exact REST call commented directly above its mock fallback.

### Expected REST contract

| Action | Method & path |
|---|---|
| Login | `POST /api/auth/login` `{ role, username, password }` → `{ token, user }` |
| Owner registers | `POST /api/owners/register` |
| List buildings (admin) | `GET /api/admin/buildings` |
| Approve/reject building | `PATCH /api/admin/buildings/{id}/status` `{ status }` |
| Admin stats | `GET /api/admin/stats` |
| List residents | `GET /api/owners/{buildingId}/residents` |
| Create resident | `POST /api/owners/{buildingId}/residents` |
| Submit meter reading | `POST /api/owners/{buildingId}/meter-readings` |
| List meter readings | `GET /api/owners/{buildingId}/meter-readings` |
| Generate bill | `POST /api/owners/{buildingId}/bills` `{ meterReadingId }` |
| List bills (owner) | `GET /api/owners/{buildingId}/bills` |
| List bills (resident) | `GET /api/residents/{id}/bills` |
| List readings (resident) | `GET /api/residents/{id}/meter-readings` |
| Create Razorpay order | `POST /api/payments/create-order` `{ billId }` |
| Verify & confirm payment | `POST /api/payments/verify` / `POST /api/payments/confirm` |

Use Spring Security + JWT: the frontend already reads the token from `login()` and attaches it as `Authorization: Bearer <token>` on every request (`src/api/axios.js`), and force-logs-out on a `401`.

### Tariff

Keep the slab table in `src/utils/tariff.js` **identical** to whatever your Spring Boot `BillingService` enforces — the frontend only uses it for an instant preview; the backend response is always the source of truth for the saved bill amount.

### Razorpay

`src/utils/razorpay.js` currently runs in `mock: true` mode (simulates a successful payment after ~1s, no real gateway call) so you can demo end-to-end immediately. Once the backend's `create-order` / `verify` endpoints exist:

1. Add your test key to `VITE_RAZORPAY_KEY_ID` in `.env`.
2. In `src/pages/resident/CurrentBill.jsx`, change `mock: true` to `mock: false`.
3. Wire `create-order` before opening checkout and `verify` inside `handler` (both endpoints are stubbed as comments in `razorpay.js`).

## Project structure

```
src/
  api/            axios instance, mock "database", service layer (single source of truth for data calls)
  components/     shared UI: DashboardShell, MeterDial, FillTube, RippleButton, Panel, StatusPill, UiBits
  context/        AuthContext (role + session)
  pages/
    auth/         Login, OwnerRegister
    superadmin/    Overview, PendingRequests, ApprovedBuildings
    owner/         Dashboard, Profile, Residents, MeterEntry, BillGeneration, PaymentStatus, Reports
    resident/      Dashboard, Profile, MeterReading, CurrentBill, PreviousBills, PaymentHistory
  utils/          tariff.js, razorpay.js, receipt.js
```

## Design notes

The visual language leans into the "meter panel" idea: a signature **meter dial** gauge (`MeterDial.jsx`) recurs everywhere usage is shown, tariff tiers render as a "water level" fill tube (`FillTube.jsx`), and primary buttons ripple on click. Palette: ink navy, flow teal, amber (pending), coral (overdue/rejected). Type: Space Grotesk (display), Inter (body), IBM Plex Mono (meter readings, amounts, IDs).
