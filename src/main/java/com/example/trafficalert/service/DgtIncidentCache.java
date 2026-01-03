package com.example.trafficalert.service;

import com.example.trafficalert.model.AlertEvent;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Primary
@Service
public class DgtIncidentCache implements AlertProvider {

    private final DgtNapClient client;
    private final DgtDatexParser parser;

    private volatile List<AlertEvent> cached = Collections.emptyList();
    private volatile long lastOkEpochMs = 0;

    public DgtIncidentCache(DgtNapClient client, DgtDatexParser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Scheduled(fixedDelay = 60_000)
    public void refresh() {
        try {
            String xml = client.downloadXml();
            cached = parser.parseToEvents(xml);
            lastOkEpochMs = System.currentTimeMillis();
            System.out.println("DGT incidents loaded: " + cached.size());
        } catch (Exception e) {
            System.err.println("DGT refresh failed: " + e.getMessage());
        }
    }

    @Override
    public List<AlertEvent> getActiveEvents() {
        return cached;
    }

    public long getLastOkEpochMs() {
        return lastOkEpochMs;
    }
}