package com.example.trafficalert.controller;

import com.example.trafficalert.model.AlertEvent;
import com.example.trafficalert.model.AlertHit;
import com.example.trafficalert.service.AlertProvider;
import com.example.trafficalert.util.Geo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class AlertsController {

    private final AlertProvider provider;

    public AlertsController(AlertProvider provider) {
        this.provider = provider;
    }

    @GetMapping("/alerts")
    public List<AlertHit> alerts(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "2000") @Min(100) @Max(20000) int radiusMeters,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit
    ) {
        List<AlertEvent> events = provider.getActiveEvents();

        return events.stream()
                .map(e -> new AlertHit(e, Geo.haversineMeters(lat, lon, e.lat(), e.lon())))
                .filter(hit -> hit.distanceMeters() <= radiusMeters)
                .sorted(Comparator.comparingDouble(AlertHit::distanceMeters))
                .limit(limit)
                .toList();
    }

    @GetMapping("/health")
    public String health() { return "ok"; }
}