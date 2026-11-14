package com.gamerlinker.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Value("${supabase.url:https://vrkkmeypnxbnpphkqmao.supabase.co}")
    private String supabaseUrl;

    @Value("${supabase.key:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZya2ttZXlwbnhibnBwaGtxbWFvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODkwNzYyMzQsImV4cCI6MjEwNDY1MjIzNH0.fRh894y4RvMVpgSGVyCn_PMg_w8sDt52WDVnl6cvs8c}")
    private String supabaseKey;

    @GetMapping
    public Map<String, String> getConfig() {
        return Map.of(
            "supabaseUrl", supabaseUrl,
            "supabaseKey", supabaseKey
        );
    }
}