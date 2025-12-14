package com.example.bookstore.health;

import com.example.bookstore.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok("OK", Map.of(
                "status", "UP",
                "version", "0.0.1",
                "timestamp", Instant.now().toString(),
                "buildTime", "unknown"
        ));
    }
}
