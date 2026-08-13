package edu.udistrital.sig.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import edu.udistrital.sig.domain.exception.InvalidEntityException;
import edu.udistrital.sig.domain.model.Category;
import edu.udistrital.sig.domain.model.EntityNature;
import edu.udistrital.sig.domain.model.GeographicEntity;
import edu.udistrital.sig.domain.repository.CategoryRepository;
import edu.udistrital.sig.infrastructure.codec.GeometryJsonCodec;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class EntityRequestMapper {

    private EntityRequestMapper() {
    }

    public static GeographicEntity toDomain(JsonNode node, String id, CategoryRepository categoryRepository) {
        String categoryReference = node.path("category").asText();
        Category category = resolveCategory(categoryReference, categoryRepository);
        EntityNature nature = resolveNature(node.path("nature").asText());

        return GeographicEntity.create(
                id,
                requiredText(node.path("name"), "El nombre de la entidad es obligatorio."),
                nullableText(node.path("description")),
                nature,
                category,
                readAttributes(node.path("attributes")),
                GeometryJsonCodec.fromJson(node.path("geometry")));
    }

    private static Category resolveCategory(String reference, CategoryRepository categoryRepository) {
        if (reference.isBlank()) {
            throw new InvalidEntityException("La categoría de la entidad es obligatoria.");
        }
        return categoryRepository.findAll().stream()
                .filter(c -> c.id().equalsIgnoreCase(reference) || c.name().equalsIgnoreCase(reference))
                .findFirst()
                .orElseThrow(() -> new InvalidEntityException("La categoría '" + reference + "' no es válida."));
    }

    private static EntityNature resolveNature(String natureReference) {
        try {
            return EntityNature.valueOf(natureReference);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEntityException("La naturaleza '" + natureReference + "' no es válida.");
        }
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

    private static String requiredText(JsonNode node, String message) {
        String text = nullableText(node);
        if (text == null) {
            throw new InvalidEntityException(message);
        }
        return text;
    }

    private static String nullableText(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String text = node.asText();
        return text.isBlank() ? null : text;
    }
}
