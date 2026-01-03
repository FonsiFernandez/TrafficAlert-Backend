package com.example.trafficalert.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReverseGeocoder {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // Caché muy simple: lat,lon redondeados → resultado
    private final Map<String, Address> cache = new ConcurrentHashMap<>();

    public Address lookup(double lat, double lon) {
        String key = String.format("%.4f,%.4f", lat, lon); // ~11 m de precisión
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        try {
            String url =
                    "https://nominatim.openstreetmap.org/reverse"
                            + "?format=jsonv2"
                            + "&lat=" + lat
                            + "&lon=" + lon
                            + "&zoom=10"
                            + "&addressdetails=1";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "TrafficAlert-MVP/0.1")
                    .GET()
                    .build();

            HttpResponse<String> res =
                    http.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() != 200) {
                return null;
            }

            JsonNode root = mapper.readTree(res.body());
            JsonNode addr = root.get("address");
            if (addr == null) {
                return null;
            }

            String province = text(addr, "state");
            String municipality =
                    firstNonNull(
                            text(addr, "city"),
                            text(addr, "town"),
                            text(addr, "village"),
                            text(addr, "municipality")
                    );

            Address a = new Address(province, municipality);
            cache.put(key, a);
            return a;

        } catch (Exception e) {
            return null;
        }
    }

    private static String text(JsonNode node, String key) {
        JsonNode v = node.get(key);
        return v == null ? null : v.asText(null);
    }

    private static String firstNonNull(String... vals) {
        for (String v : vals) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    public record Address(String province, String municipality) {}
}