package com.iris.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iris.backend.util.ApiResponse;

/**
 * Liveness check endpoint.
 *
 * <p>
 * Useful for health probes (load balancers, Docker, Kubernetes).
 * This endpoint is intentionally unauthenticated.
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ApiResponse.success("OK");
    }
}
