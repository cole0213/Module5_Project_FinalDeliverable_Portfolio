package com.group3.burstytraffic.model;

/**
 * Serialized to JSON and (a) pushed over STOMP to /topic/metrics every tick,
 * and (b) returned from each /api/* POST. Field names must match the frontend
 * (m.rps, m.dropped, m.processed, m.queueSize, m.status, m.onPremiseServers,
 *  m.cloudServers, m.cloudBurstingEnabled, m.waitingRoomEnabled).
 */
public class Metrics {
    private int rps;
    private long dropped;
    private long processed;
    private int queueSize;
    private String status;
    private int onPremiseServers;
    private int cloudServers;
    private boolean cloudBurstingEnabled;
    private boolean waitingRoomEnabled;

    public Metrics() { }

    public int getRps() { return rps; }
    public void setRps(int rps) { this.rps = rps; }

    public long getDropped() { return dropped; }
    public void setDropped(long dropped) { this.dropped = dropped; }

    public long getProcessed() { return processed; }
    public void setProcessed(long processed) { this.processed = processed; }

    public int getQueueSize() { return queueSize; }
    public void setQueueSize(int queueSize) { this.queueSize = queueSize; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getOnPremiseServers() { return onPremiseServers; }
    public void setOnPremiseServers(int onPremiseServers) { this.onPremiseServers = onPremiseServers; }

    public int getCloudServers() { return cloudServers; }
    public void setCloudServers(int cloudServers) { this.cloudServers = cloudServers; }

    public boolean isCloudBurstingEnabled() { return cloudBurstingEnabled; }
    public void setCloudBurstingEnabled(boolean cloudBurstingEnabled) { this.cloudBurstingEnabled = cloudBurstingEnabled; }

    public boolean isWaitingRoomEnabled() { return waitingRoomEnabled; }
    public void setWaitingRoomEnabled(boolean waitingRoomEnabled) { this.waitingRoomEnabled = waitingRoomEnabled; }
}
