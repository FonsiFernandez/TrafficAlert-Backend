package com.example.trafficalert.model;

public record AlertHit(
        AlertEvent event,
        double distanceMeters
) {}
