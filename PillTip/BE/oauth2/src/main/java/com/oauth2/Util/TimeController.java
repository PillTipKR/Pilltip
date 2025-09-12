package com.oauth2.Util;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class TimeController {
    @GetMapping("/api/server-time")
    public Map<String, Long> getServerTime() {
        return Map.of("serverTime", System.currentTimeMillis());
    }
} 