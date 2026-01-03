package com.example.trafficalert.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class DgtNapClient {

    private static final String FEED =
            "https://nap.dgt.es/datex2/v3/dgt/SituationPublication/datex2_v36.xml";

    private final HttpClient http = HttpClient.newBuilder().build();

    public String downloadXml() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(FEED))
                .GET()
                .header("Accept", "application/xml")
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new RuntimeException("DGT NAP HTTP " + res.statusCode());
        }
        return res.body();
    }
}