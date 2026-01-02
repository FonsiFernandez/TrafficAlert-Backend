package com.example.trafficalert.service;

import com.example.trafficalert.model.AlertEvent;
import java.util.List;

public interface AlertProvider {
    List<AlertEvent> getActiveEvents();
}
