package com.group3.burstytraffic.service;

import com.group3.burstytraffic.model.Metrics;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Drives the live simulation. Every second it:
 *   1. generates incoming demand (normal ~25-80 RPS, or burst ~1000-1200 RPS),
 *   2. optionally scales cloud capacity (with a cold-start delay),
 *   3. routes traffic through the waiting-room queue (if enabled) or drops the
 *      overflow, modelling a hard collapse when an unprotected system is
 *      severely overloaded,
 *   4. pushes a Metrics snapshot to /topic/metrics.
 *
 * Capacity model matches the frontend: RPS_PER_THREAD = 50, so 3 on-prem
 * servers = 150 RPS, and 3 + up to 17 cloud = 1000 RPS.
 */
@Service
public class SimulationService {

    private static final int RPS_PER_THREAD = 50;
    private static final int ON_PREM        = 3;
    private static final int MAX_CLOUD      = 17;
    private static final int MAX_QUEUE      = 5000;
    private static final int COLD_START_TICKS = 2;   // ~2s detect + spin-up delay

    private final SimpMessagingTemplate messaging;
    private final Random rnd = new Random();

    // mutable state
    private int     cloud        = 0;
    private boolean cloudEnabled = false;
    private boolean waitEnabled  = false;
    private boolean bursting     = false;
    private int     queue        = 0;
    private long    dropped      = 0;
    private long    processed    = 0;
    private int     rps          = 0;
    private int     coldStart    = 0;
    private String  status       = "NORMAL";

    public SimulationService(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @Scheduled(fixedRate = 1000)
    public void tick() {
        Metrics m = step();
        messaging.convertAndSend("/topic/metrics", m);
    }

    private synchronized Metrics step() {
        int incoming = bursting
                ? 1000 + rnd.nextInt(201)   // 1000-1200 RPS
                : 25 + rnd.nextInt(56);     // 25-80 RPS

        int capacity = (ON_PREM + cloud) * RPS_PER_THREAD;

        // ── Cloud bursting: expand the supply side ──────────────────────────
        if (cloudEnabled) {
            if (incoming > capacity && cloud < MAX_CLOUD) {
                coldStart++;
                if (coldStart >= COLD_START_TICKS) {           // cold-start gap
                    cloud = Math.min(MAX_CLOUD, cloud + 4);
                    coldStart = 0;
                }
            } else if (!bursting && incoming < capacity * 0.4 && cloud > 0) {
                cloud = Math.max(0, cloud - 4);                // release after burst
                coldStart = 0;
            } else {
                coldStart = 0;
            }
        } else {
            cloud = 0;
            coldStart = 0;
        }
        capacity = (ON_PREM + cloud) * RPS_PER_THREAD;         // recompute post-scale

        // ── Admission: smooth the demand side (or collapse) ─────────────────
        int served;
        int droppedNow;
        boolean collapsed = false;

        if (waitEnabled) {
            int pending  = queue + incoming;
            served       = Math.min(capacity, pending);
            int overflow = pending - served;                   // stays in queue
            int newQueue = Math.min(overflow, MAX_QUEUE);
            droppedNow   = overflow - newQueue;                // queue full -> door drops
            queue        = newQueue;
        } else {
            queue = 0;
            if (!cloudEnabled && incoming > capacity * 3) {
                // unprotected severe overload: the server dies, it does not slow down
                collapsed  = true;
                served     = (int) (capacity * 0.1);           // thrashing
                droppedNow = incoming - served;
            } else {
                served     = Math.min(incoming, capacity);
                droppedNow = Math.max(0, incoming - served);
            }
        }

        dropped   += droppedNow;
        processed += served;
        rps        = served;

        if (collapsed)                          status = "DOWN";
        else if (bursting && cloud > 0)         status = "SCALING";
        else if (bursting)                      status = "BURST";
        else if (rps > capacity * 0.9)          status = "OVERLOAD";
        else                                    status = "NORMAL";

        return snapshot();
    }

    private Metrics snapshot() {
        Metrics m = new Metrics();
        m.setRps(rps);
        m.setDropped(dropped);
        m.setProcessed(processed);
        m.setQueueSize(queue);
        m.setStatus(status);
        m.setOnPremiseServers(ON_PREM);
        m.setCloudServers(cloud);
        m.setCloudBurstingEnabled(cloudEnabled);
        m.setWaitingRoomEnabled(waitEnabled);
        return m;
    }

    // ── Actions invoked by the REST controller ─────────────────────────────
    public synchronized Metrics startBurst()        { bursting = true;  return snapshot(); }
    public synchronized Metrics stopBurst()         { bursting = false; return snapshot(); }
    public synchronized Metrics toggleCloud()       { cloudEnabled = !cloudEnabled; return snapshot(); }
    public synchronized Metrics toggleWaitingRoom() { waitEnabled  = !waitEnabled;  return snapshot(); }

    public synchronized Metrics reset() {
        cloud = 0; cloudEnabled = false; waitEnabled = false; bursting = false;
        queue = 0; dropped = 0; processed = 0; rps = 0; coldStart = 0;
        status = "NORMAL";
        return snapshot();
    }
}
