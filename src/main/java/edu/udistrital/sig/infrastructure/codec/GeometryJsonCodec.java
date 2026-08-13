package edu.udistrital.sig.infrastructure.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.udistrital.sig.domain.exception.InvalidGeometryException;
import edu.udistrital.sig.domain.model.Coordinate;
import edu.udistrital.sig.domain.model.Geometry;
import edu.udistrital.sig.domain.model.LineString;
import edu.udistrital.sig.domain.model.Point;
import edu.udistrital.sig.domain.model.Polygon;

import java.util.ArrayList;
import java.util.List;

public final class GeometryJsonCodec {

    private GeometryJsonCodec() {
    }

    public static Geometry fromJson(JsonNode node) {
        String type = node.path("type").asText();
        JsonNode coordinates = node.path("coordinates");
        return switch (type) {
            case "Point" -> new Point(readCoordinate(coordinates));
            case "LineString" -> new LineString(readCoordinates(coordinates));
            case "Polygon" -> new Polygon(readCoordinates(coordinates.path(0)));
            default -> throw new InvalidGeometryException("Tipo de geometría no soportado: " + type);
        };
    }

    public static ObjectNode toJson(Geometry geometry) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("type", geometry.type());
        if (geometry instanceof Point point) {
            node.set("coordinates", writeCoordinate(point.coordinate()));
        } else if (geometry instanceof LineString lineString) {
            node.set("coordinates", writeCoordinates(lineString.coordinates()));
        } else if (geometry instanceof Polygon polygon) {
            ArrayNode rings = node.arrayNode();
            rings.add(writeCoordinates(polygon.ring()));
            node.set("coordinates", rings);
        }
        return node;
    }

    private static Coordinate readCoordinate(JsonNode node) {
        double longitude = node.path(0).asDouble();
        double latitude = node.path(1).asDouble();
        return Coordinate.ofLonLat(longitude, latitude);
    }

    private static List<Coordinate> readCoordinates(JsonNode node) {
        List<Coordinate> coordinates = new ArrayList<>();
        node.forEach(item -> coordinates.add(readCoordinate(item)));
        return coordinates;
    }

    private static ArrayNode writeCoordinate(Coordinate coordinate) {
        return JsonNodeFactory.instance.arrayNode()
                .add(coordinate.longitude())
                .add(coordinate.latitude());
    }

    private static ArrayNode writeCoordinates(List<Coordinate> coordinates) {
        ArrayNode node = JsonNodeFactory.instance.arrayNode();
        coordinates.forEach(c -> node.add(writeCoordinate(c)));
        return node;
    }
}
