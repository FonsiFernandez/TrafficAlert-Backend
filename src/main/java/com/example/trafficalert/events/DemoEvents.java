package com.example.trafficalert.events;

import com.example.trafficalert.model.AlertEvent;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class DemoEvents {

    public static List<AlertEvent> near(double lat, double lon) {
        double lat2 = lat + 0.002; // un poco más al norte

        return List.of(
                new AlertEvent(
                        "demo-" + UUID.randomUUID(), // id
                        "ACCIDENT",                  // type
                        "Advertencia",               // title
                        "Incidencia de prueba",      // cause
                        "DEMO-ROAD",                 // road
                        "PK 0.0",                    // pkText
                        0.0,                         // pkKm
                        "Creciente",                 // direction
                        "Norte",                     // orientation
                        "Provincia demo",            // province
                        "Municipio demo",            // municipality
                        OffsetDateTime.now().toString(), // startTime
                        "DEMO",                      // source
                        lat2,                        // lat
                        lon,                         // lon
                        4                            // severity
                )
        );
    }
}
