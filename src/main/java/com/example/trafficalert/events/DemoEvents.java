package com.example.trafficalert.events;

import com.example.trafficalert.model.AlertEvent;

import java.util.List;
import java.util.UUID;

public class DemoEvents {
    public static List<AlertEvent> near(double lat, double lon) {
        // ~300m al norte (aprox)
        double lat2 = lat + 0.0027;

        return List.of(
                new AlertEvent("demo-" + UUID.randomUUID(), "ACCIDENT", "Accidente (DEMO)", "Incidencia falsa para pruebas", lat2, lon, 4)
        );
    }
}