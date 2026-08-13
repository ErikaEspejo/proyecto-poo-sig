package edu.udistrital.sig.infrastructure.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import edu.udistrital.sig.domain.model.Category;
import edu.udistrital.sig.domain.model.EntityNature;
import edu.udistrital.sig.domain.model.GeographicEntity;
import edu.udistrital.sig.domain.model.Geometry;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EntityJsonCodec {

    private EntityJsonCodec() {
    }

    public static GeographicEntity fromJson(JsonNode node, Category category) {
        Map<String, String> attributes = readAttributes(node.path("attributes"));
        Geometry geometry = GeometryJsonCodec.fromJson(node.path("geometry"));

        return GeographicEntity.create(
                node.path("id").asText(),
                node.path("name").asText(),
                nullableText(node.path("description")),
                EntityNature.valueOf(node.path("nature").asText()),
                category,
                attributes,
                geometry);
    }

    public static ObjectNode toJson(GeographicEntity entity) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("id", entity.id());
        node.put("name", entity.name());
        if (entity.description() != null) {
            node.put("description", entity.description());
        }
        node.put("nature", entity.nature().name());
        node.put("category", entity.category().id());
        node.set("attributes", attributesToJson(entity.attributes()));
        node.set("geometry", GeometryJsonCodec.toJson(entity.geometry()));
        return node;
    }

    private static Map<String, String> readAttributes(JsonNode node) {
        Map<String, String> attributes = new LinkedHashMap<>();
        if (node == null || node.isNull()) {
            return attributes;
        }
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            attributes.put(entry.getKey(), entry.getValue().asText());
        }
        return attributes;
    }

    private static ObjectNode attributesToJson(Map<String, String> attributes) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        attributes.forEach(node::put);
        return node;
    }

    private static String nullableText(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String text = node.asText();
        return text.isBlank() ? null : text;
    }
}
