# Demo — Bursty Traffic Load Simulation

A live, interactive simulation of a 3-server system under a sudden traffic burst,
with toggles for **Hybrid Cloud Bursting** and a **Virtual Waiting Room**.

🕹️ **Live deployment:** https://bursty-traffic-demo.onrender.com/
*(Render free tier — first request may take 30–60s to wake from sleep.)*

## What it does

- Renders a real-time RPS (requests/second) chart against a fixed `capacity = 150` line.
- A **🎫 티켓 오픈!** button triggers a burst of ~1,000–1,200 RPS (≈8× capacity).
- **Cloud Bursting toggle** — spins up to ~17 dynamic VMs when load crosses a threshold, raising effective capacity, then releases them after the burst.
- **Waiting Room toggle** — queues excess users (FIFO, with position + ETA) and admits them at a rate the backend can absorb.
- Live counter of **dropped requests**, so you can compare the three scenarios from the write-up:
  - Burst, no protection → ~145 dropped (service dies)
  - Burst, both on → ~30 dropped (service survives)
  - Normal load, waiting room on → 0 dropped

## Structure

```
demo/
├── backend/    # Server model, capacity + auto-scaler logic, FIFO queue, RPS metering
└── frontend/   # Real-time RPS dashboard, toggles, and the "티켓 오픈!" burst trigger
```

## Running locally

> Add the source for the deployed app under `backend/` and `frontend/`, then document
> the exact run command here (e.g. `python backend/app.py` and open the frontend, or a
> single `npm start` / `flask run`, depending on your stack).
