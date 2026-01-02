package com.example.trafficalert.model;

public record AlertEvent(
        String id,
        String type,        // "ACCIDENT", "JAM", "ROAD_CLOSED"
        String title,       // "Accidente"
        String description, // "Incidente reportado"
        double lat,
        double lon,
        Integer severity    // 1-5 opcional
) {}
