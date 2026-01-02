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
                new AlertEvent("acc-001","ACCIDENT","Accidente","Accidente reportado",40.4169,-3.7036,4),
                new AlertEvent("jam-002","JAM","Retención","Tráfico denso",40.4200,-3.7050,2),
                new AlertEvent("clo-003","ROAD_CLOSED","Corte","Carril cerrado",40.4300,-3.7000,3)
        );
    }
}