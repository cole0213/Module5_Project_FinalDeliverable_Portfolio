# Demo — Bursty Traffic Load Simulation

A live, interactive simulation of a 3-server system under a sudden traffic burst,
with toggles for **Hybrid Cloud Bursting** and a **Virtual Waiting Room**.

🕹️ **Live deployment:** https://bursty-traffic-demo.onrender.com/
*(Render free tier — first request may take 30–60s to wake from sleep.)*

> ℹ️ The backend in `backend/` was reconstructed to match the **exact contract**
> used by the recovered frontend — the SockJS endpoint, STOMP topic, REST routes,
> and metrics JSON shape all line up, so it is a drop-in, functionally-equivalent
> replacement for the deployed service.

## Architecture

```
Browser (index.html)                       Spring Boot backend
┌──────────────────────┐                   ┌───────────────────────────────┐
│ Chart.js dashboard    │  STOMP /topic/    │ SimulationService (@Scheduled) │
│  ▲ live RPS curve     │ ◀── metrics ───── │  tick() every 1s:              │
│                       │   (SockJS /ws)    │   • generate demand            │
│ toggles + 티켓 오픈!   │  ── POST /api/* ─▶ │   • cloud bursting (supply)    │
└──────────────────────┘                   │   • waiting room (demand)      │
                                            │ ApiController  → returns JSON  │
                                            └───────────────────────────────┘
```

**Endpoints (frontend ⇄ backend contract):**

| Direction | Channel | Purpose |
|---|---|---|
| Server → client | STOMP topic `/topic/metrics` (over SockJS `/ws`) | Pushes a metrics snapshot every second |
| Client → server | `POST /api/burst/start` · `/api/burst/stop` | Trigger / stop the burst |
| Client → server | `POST /api/cloud-bursting/toggle` | Enable / disable cloud bursting |
| Client → server | `POST /api/waiting-room/toggle` | Enable / disable the waiting room |
| Client → server | `POST /api/reset` | Reset all state |

**Metrics JSON:** `rps`, `dropped`, `processed`, `queueSize`, `status`,
`onPremiseServers`, `cloudServers`, `cloudBurstingEnabled`, `waitingRoomEnabled`.

**Capacity model:** `RPS_PER_THREAD = 50`, so 3 on-prem servers = 150 RPS, and
3 + up to 17 cloud VMs = 1000 RPS. Normal demand is ~25–80 RPS; a burst is
~1,000–1,200 RPS (≈8× the on-prem ceiling).

## Structure

```
backend/
├── Dockerfile                         # Render / container deploy
├── pom.xml                            # Spring Boot 3.3, Java 17
└── src/main/
    ├── java/com/group3/burstytraffic/
    │   ├── BurstyTrafficApplication.java
    │   ├── config/WebSocketConfig.java     # SockJS /ws + STOMP /topic
    │   ├── model/Metrics.java              # JSON payload
    │   ├── service/SimulationService.java  # traffic + scaling + queue logic
    │   └── web/ApiController.java          # /api/* routes
    └── resources/
        ├── application.properties
        └── static/index.html               # the recovered frontend (served at /)
```

## Run locally

```bash
cd backend
./mvnw spring-boot:run      # or: mvn spring-boot:run  (Maven + JDK 17 required)
```

Then open <http://localhost:8080>. Hit **🎫 티켓 오픈!** to trigger the burst and
toggle the two defenses to compare the three scenarios from the write-up:

| Scenario | Toggles | Expected dropped |
|---|---|---|
| Burst, no protection | both OFF | large (service collapses → `status: DOWN`) |
| Burst, both on | cloud + waiting room ON | small (service survives) |
| Normal load, waiting room on | waiting room ON, no burst | 0 |

## Deploy on Render

The included `Dockerfile` builds and runs the jar. On Render: **New → Web Service**,
point it at this repo, set **Root Directory** to `demo/backend`, choose the
**Docker** runtime. Render injects `PORT` automatically (the app reads it).
