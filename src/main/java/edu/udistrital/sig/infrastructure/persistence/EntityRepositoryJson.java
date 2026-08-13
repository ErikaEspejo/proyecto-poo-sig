package edu.udistrital.sig.infrastructure.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.udistrital.sig.domain.model.Category;
import edu.udistrital.sig.domain.model.GeographicEntity;
import edu.udistrital.sig.domain.exception.InvalidEntityException;
import edu.udistrital.sig.domain.repository.CategoryRepository;
import edu.udistrital.sig.domain.repository.EntityRepository;
import edu.udistrital.sig.infrastructure.codec.EntityJsonCodec;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class EntityRepositoryJson implements EntityRepository {

    private static final String FILE_NAME = "entities.json";

    private final JsonDataStore store;
    private final CategoryRepository categoryRepository;

    public EntityRepositoryJson(JsonDataStore store, CategoryRepository categoryRepository) {
        this.store = store;
        this.categoryRepository = categoryRepository;
    }
    @Override
    public List<GeographicEntity> findAll() {
        List<GeographicEntity> entities = new ArrayList<>();
        store.readArray(FILE_NAME).forEach(node ->
                entities.add(EntityJsonCodec.fromJson(node, resolveCategory(node))));
        return entities;
    }

    @Override
    public Optional<GeographicEntity> findById(String id) {
        for (JsonNode node : store.readArray(FILE_NAME)) {
            if (id.equals(node.path("id").asText())) {
                return Optional.of(EntityJsonCodec.fromJson(node, resolveCategory(node)));
            }
        }
        return Optional.empty();
    }

    @Override
    public GeographicEntity save(GeographicEntity entity) {
        ArrayNode updated = store.readArray(FILE_NAME);
        JsonNode stored = EntityJsonCodec.toJson(entity);
        for (int i = 0; i < updated.size(); i++) {
            if (entity.id().equals(updated.get(i).path("id").asText())) {
                updated.remove(i);
                i--;
            }
        }
        updated.add(stored);
        store.write(FILE_NAME, updated);
        return entity;
    }

    @Override
    public void delete(String id) {
        ArrayNode updated = store.readArray(FILE_NAME);
        for (int i = 0; i < updated.size(); i++) {
            if (id.equals(updated.get(i).path("id").asText())) {
                updated.remove(i);
                i--;
            }
        }
        store.write(FILE_NAME, updated);
    }

    private Category resolveCategory(JsonNode node) {
        String categoryId = node.path("category").asText();
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new InvalidEntityException("La categoría " + categoryId + " no existe."));
    }
}
