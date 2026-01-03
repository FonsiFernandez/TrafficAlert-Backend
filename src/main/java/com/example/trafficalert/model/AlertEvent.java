package com.example.trafficalert.model;

public record AlertEvent(
        String id,
        String type,            // ACCIDENT, VEHICLE_STOPPED, ROADWORKS, etc.
        String title,           // "Advertencia"
        String cause,           // "Vehículo detenido"
        String road,            // "PO-403"
        String pkText,          // "PK 10.7" (texto tal cual)
        Double pkKm,            // 10.7 (si lo puedes parsear)
        String direction,       // "Creciente" / "Decreciente" o similar
        String orientation,     // "Norte" / "Sur" / etc.
        String province,        // "Pontevedra"
        String municipality,    // "Ponteareas"
        String startTime,       // ISO string, ej "2026-01-03T03:25:00+01:00"
        String source,          // "DGT3.0" o "NAP"
        double lat,
        double lon,
        Integer severity
) {}
