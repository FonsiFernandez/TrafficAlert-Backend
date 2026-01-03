package com.example.trafficalert.service;

import com.example.trafficalert.model.AlertEvent;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DgtDatexParser {

    public List<AlertEvent> parseToEvents(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        Document doc = dbf.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        XPath xp = XPathFactory.newInstance().newXPath();

        NodeList records = (NodeList) xp.evaluate(
                "//*[local-name()='situationRecord']",
                doc,
                XPathConstants.NODESET
        );

        List<AlertEvent> out = new ArrayList<>();

        for (int i = 0; i < records.getLength(); i++) {
            var node = records.item(i);

            String id = xp.evaluate("./@id", node);
            if (id == null || id.isBlank()) id = "dgt-" + i;

            String latStr = xp.evaluate(".//*[local-name()='latitude']/text()", node);
            String lonStr = xp.evaluate(".//*[local-name()='longitude']/text()", node);
            if (isBlank(latStr) || isBlank(lonStr)) continue;

            double lat = Double.parseDouble(latStr);
            double lon = Double.parseDouble(lonStr);

            // ✅ Tipo fiable: usa el xsi:type si existe, si no usa local-name del propio elemento.
            // En muchos DATEX2, situationRecord es un elemento con atributos incluyendo xsi:type.
            String xsiType = xp.evaluate("./@*[local-name()='type']", node);
            String recordType = !isBlank(xsiType) ? xsiType : xp.evaluate("local-name(.)", node);
            String type = mapType(recordType);

            String title = switch (type) {
                case "ACCIDENT" -> "Accidente";
                case "JAM" -> "Retención";
                case "ROADWORKS" -> "Obras";
                case "VEHICLE_STOPPED" -> "Vehículo detenido";
                default -> "Incidencia";
            };

            // Cause / comments: depende mucho del registro; probamos varias rutas comunes
            String cause = deriveCause(xp, node, recordType);

            if (cause == null) {
                // fallback a comentarios si no hay nada mejor
                cause = firstNonBlank(
                        xp.evaluate(".//*[local-name()='causeDescription']/text()", node),
                        xp.evaluate(".//*[local-name()='comment']/text()", node),
                        xp.evaluate(".//*[local-name()='generalPublicComment']//*[local-name()='comment']/text()", node),
                        xp.evaluate(".//*[local-name()='nonGeneralPublicComment']//*[local-name()='comment']/text()", node)
                );
            }


            // Road: a veces viene en referencedLocation / roadNumber / roadName
            String road =
                    firstNonBlank(
                            xp.evaluate(".//*[local-name()='roadNumber']/text()", node),
                            xp.evaluate(".//*[local-name()='roadName']/text()", node),
                            xp.evaluate(".//*[local-name()='name']/text()", node)
                    );

            // Direction: muy variable; intentamos lo típico
            String direction =
                    firstNonBlank(
                            xp.evaluate(".//*[local-name()='direction']/text()", node),
                            xp.evaluate(".//*[local-name()='trafficDirection']/text()", node),
                            xp.evaluate(".//*[local-name()='directionOfTravel']/text()", node)
                    );

            // Orientation: rarísimo en NAP, suele ser null
            String orientation = null;

            // Start time: también varía
            String startTime =
                    firstNonBlank(
                            xp.evaluate(".//*[local-name()='situationRecordCreationTime']/text()", node),
                            xp.evaluate(".//*[local-name()='situationRecordVersionTime']/text()", node),
                            xp.evaluate(".//*[local-name()='versionTime']/text()", node)
                    );

            // PK: normalmente no viene “bonito” en NAP → lo dejamos null
            String pkText = null;
            Double pkKm = null;

            // Provincia / municipio: NAP normalmente no lo trae directo → null (luego lo enriqueces)
            String province = null;
            String municipality = null;

            // Severity: si no existe, null
            Integer severity = null;

            out.add(new AlertEvent(
                    id,
                    type,
                    title,
                    cause,
                    road,
                    pkText,
                    pkKm,
                    direction,
                    orientation,
                    province,
                    municipality,
                    startTime,
                    "NAP",
                    lat,
                    lon,
                    severity
            ));
        }

        return out;
    }

    private String mapType(String raw) {
        if (raw == null) return "INCIDENT";
        String t = raw.toLowerCase();

        if (t.contains("accident")) return "ACCIDENT";
        if (t.contains("abnormaltraffic") || t.contains("trafficstatus")) return "JAM";
        if (t.contains("roadworks")) return "ROADWORKS";
        if (t.contains("vehicleobstruction")) return "VEHICLE_STOPPED";
        if (t.contains("generalobstruction")) return "INCIDENT";

        return "INCIDENT";
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) {
            if (v != null && !v.isBlank()) return v.trim();
        }
        return null;
    }

    private String deriveCause(XPath xp, org.w3c.dom.Node node, String recordType) throws Exception {
        if (recordType == null) return null;
        String t = recordType.toLowerCase();

        // 1) Accidentes
        if (t.contains("accident")) {
            String accidentType = xp.evaluate(".//*[local-name()='accidentType']/text()", node);
            if (!isBlank(accidentType)) return humanize(accidentType); // ej: collision -> Collision
            return "Accidente";
        }

        // 2) Tráfico anómalo / retenciones
        if (t.contains("abnormaltraffic") || t.contains("trafficstatus")) {
            String abnormal = xp.evaluate(".//*[local-name()='abnormalTrafficType']/text()", node);
            if (!isBlank(abnormal)) return humanize(abnormal); // stationaryTraffic -> Stationary Traffic
            return "Retención";
        }

        // 3) Obras
        if (t.contains("roadworks")) {
            String worksType = xp.evaluate(".//*[local-name()='roadworksType']/text()", node);
            if (!isBlank(worksType)) return humanize(worksType);
            return "Obras";
        }

        // 4) Obstáculos
        if (t.contains("vehicleobstruction")) {
            return "Vehículo detenido";
        }
        if (t.contains("animalpresenceobstruction")) {
            return "Animal en la calzada";
        }
        if (t.contains("generalobstruction")) {
            String obstructionType = xp.evaluate(".//*[local-name()='obstructionType']/text()", node);
            if (!isBlank(obstructionType)) return humanize(obstructionType);
            return "Obstrucción en la calzada";
        }

        // 5) Meteorología
        if (t.contains("weatherrelatedroadconditions")) {
            String w = xp.evaluate(".//*[local-name()='weatherRelatedRoadConditionType']/text()", node);
            if (!isBlank(w)) return humanize(w);
            return "Condiciones meteorológicas";
        }

        return null;
    }

    private String humanize(String s) {
        if (s == null) return null;
        // camelCase -> "Camel Case"
        String spaced = s.replaceAll("([a-z])([A-Z])", "$1 $2");
        // primera mayúscula
        return spaced.substring(0, 1).toUpperCase() + spaced.substring(1);
    }
}
