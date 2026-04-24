package com.domus.server.health.controller;

import com.domus.server.common.api.ApiResponse;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final Environment environment;

    public HealthController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        String profile = environment.getActiveProfiles().length == 0 ? "default" : environment.getActiveProfiles()[0];

        return ApiResponse.of(Map.of(
            "application", "domus-server",
            "profile", profile,
            "status", "ok"
        ));
    }
}
