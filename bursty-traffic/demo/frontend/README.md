# Frontend — Real-time RPS Dashboard

A single-page dashboard that visualizes the bursty-traffic simulation in real time:
a live requests-per-second (RPS) curve against the server capacity line, a
utilization bar, per-server boxes, and the dropped/processed/queue counters.

> ℹ️ This is the canonical copy of the frontend. The Spring Boot backend serves
> an identical copy from `../backend/src/main/resources/static/index.html`, so
> opening the running app at `/` renders this page. Keep the two in sync if you
> edit one.

## Stack

| Library | Version | Loaded from | Purpose |
|---|---|---|---|
| Chart.js | 4.4.1 | cdnjs | Live RPS / capacity line chart |
| SockJS | 1.6.1 | cdnjs | WebSocket transport with fallback |
| STOMP.js | 2.3.3 | cdnjs | Messaging protocol over the socket |

All three are pulled from the CDN at runtime (`<script src="https://cdnjs...">`),
so there are no local dependencies to install.

## How it talks to the backend

```
            POST /api/burst/start | /api/burst/stop
            POST /api/cloud-bursting/toggle
 index.html ── POST /api/waiting-room/toggle ──▶  Spring Boot
            POST /api/reset                        backend
            ◀── STOMP /topic/metrics (over SockJS /ws) ──
```

- On load, it opens `new SockJS('/ws')`, wraps it with STOMP, and subscribes to
  **`/topic/metrics`**. Every metrics frame the server pushes updates the chart,
  utilization bar, server boxes, and counters (`updateUI`).
- Each button (`🎫 티켓 오픈!`, the two toggles, reset) fires a `POST` to the
  matching `/api/*` route via `postAction()`, and the JSON response updates the UI
  immediately.

**Metrics fields consumed:** `rps`, `dropped`, `processed`, `queueSize`,
`status`, `onPremiseServers`, `cloudServers`, `cloudBurstingEnabled`,
`waitingRoomEnabled`. Capacity is computed client-side as
`(onPremiseServers + cloudServers) × 50`.

## Running

The frontend is served by the backend — there is no separate build step. Start
the backend (`cd ../backend && mvn spring-boot:run`) and open
<http://localhost:8080>. The paths (`/ws`, `/api/*`) are relative, so it works on
localhost and on the deployed Render URL without changes.
