package com.group3.burstytraffic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bursty Traffic load-simulation backend.
 *
 * NOTE: this backend was reconstructed to match the exact contract used by the
 * recovered frontend (SockJS '/ws', STOMP topic '/topic/metrics', and the
 * '/api/*' REST endpoints). It is functionally equivalent to the deployed demo.
 */
@SpringBootApplication
@EnableScheduling
public class BurstyTrafficApplication {
    public static void main(String[] args) {
        SpringApplication.run(BurstyTrafficApplication.class, args);
    }
}
