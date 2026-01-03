package com.example.trafficalert.service;

import com.example.trafficalert.model.AlertEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MockAlertProvider implements AlertProvider {
    @Override
    public List<AlertEvent> getActiveEvents() {
        // Coordenadas de ejemplo (Madrid centro aprox). Cambia a tu zona.
        return List.of(
                new AlertEvent(
                        "mock-1","INCIDENT","Incidencia","Mock","A-1",null,null,null,null,null,null,null,"MOCK",
                        40.4168,-3.7038,null
                )
        );
    }
}