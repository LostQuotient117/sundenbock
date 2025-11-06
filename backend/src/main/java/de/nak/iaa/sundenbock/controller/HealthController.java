package de.nak.iaa.sundenbock.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * Simple health endpoint to verify the service is running.
 */
@RestController
public class HealthController {

    /**
     * Returns a minimal health status payload.
     * @return a map with key "status" and value "UP"
     */
    @GetMapping("/api/health")
    public Map<String,String> health() { return Map.of("status","UP"); }
}