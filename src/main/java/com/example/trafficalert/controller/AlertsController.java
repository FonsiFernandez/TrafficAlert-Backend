package com.example.trafficalert.controller;

import com.example.trafficalert.events.DemoEvents;
import com.example.trafficalert.model.AlertEvent;
import com.example.trafficalert.model.AlertHit;
import com.example.trafficalert.service.AlertProvider;
import com.example.trafficalert.service.ReverseGeocoder;
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
    private final ReverseGeocoder reverseGeocoder;

    public AlertsController(AlertProvider provider, ReverseGeocoder reverseGeocoder) {
        this.provider = provider;
        this.reverseGeocoder = reverseGeocoder;
    }

    @GetMapping("/alerts")
    public List<AlertHit> alerts(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "2000") @Min(100) @Max(20000) int radiusMeters,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit,
            @RequestParam(defaultValue = "false") boolean demo
    ) {

        List<AlertEvent> events = demo
                ? DemoEvents.near(lat, lon)
                : provider.getActiveEvents();

        return events.stream()
                .map(e -> new AlertHit(e, Geo.haversineMeters(lat, lon, e.lat(), e.lon())))
                .filter(hit -> hit.distanceMeters() <= radiusMeters)
                .sorted(Comparator.comparingDouble(AlertHit::distanceMeters))
                .limit(limit)
                .map(hit -> new AlertHit(enrich(hit.event()), hit.distanceMeters()))
                .toList();
    }

    private AlertEvent enrich(AlertEvent e) {
        var addr = reverseGeocoder.lookup(e.lat(), e.lon());

        String province = addr != null ? addr.province() : null;
        String municipality = addr != null ? addr.municipality() : null;

        // Si no trae nada nuevo, devuelve el mismo evento
        if (province == null && municipality == null) return e;

        return new AlertEvent(
                e.id(),
                e.type(),
                e.title(),
                e.cause(),
                e.road(),
                e.pkText(),
                e.pkKm(),
                e.direction(),
                e.orientation(),
                province,
                municipality,
                e.startTime(),
                e.source(),
                e.lat(),
                e.lon(),
                e.severity()
        );
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
