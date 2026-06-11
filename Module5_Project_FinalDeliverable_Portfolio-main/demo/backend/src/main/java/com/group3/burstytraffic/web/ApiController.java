package com.group3.burstytraffic.web;

import com.group3.burstytraffic.model.Metrics;
import com.group3.burstytraffic.service.SimulationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints called by the frontend via postAction(path):
 *   POST /api/burst/start
 *   POST /api/burst/stop
 *   POST /api/cloud-bursting/toggle
 *   POST /api/waiting-room/toggle
 *   POST /api/reset
 * Each returns the current Metrics as JSON.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final SimulationService sim;

    public ApiController(SimulationService sim) {
        this.sim = sim;
    }

    @PostMapping("/burst/start")
    public Metrics startBurst() { return sim.startBurst(); }

    @PostMapping("/burst/stop")
    public Metrics stopBurst() { return sim.stopBurst(); }

    @PostMapping("/cloud-bursting/toggle")
    public Metrics toggleCloud() { return sim.toggleCloud(); }

    @PostMapping("/waiting-room/toggle")
    public Metrics toggleWaitingRoom() { return sim.toggleWaitingRoom(); }

    @PostMapping("/reset")
    public Metrics reset() { return sim.reset(); }
}
