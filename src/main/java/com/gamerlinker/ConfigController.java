package com.gamerlinker.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @GetMapping
    public Map<String, String> getConfig() {
        return Map.of(
            "supabaseUrl", supabaseUrl,
            "supabaseKey", supabaseKey
        );
    }
}
