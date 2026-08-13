package edu.udistrital.sig.infrastructure.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import edu.udistrital.sig.domain.model.Category;
import edu.udistrital.sig.domain.repository.CategoryRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CategoryRepositoryJson implements CategoryRepository {

    private static final String FILE_NAME = "categories.json";

    private final JsonDataStore store;

    public CategoryRepositoryJson(JsonDataStore store) {
        this.store = store;
    }

    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        store.read(FILE_NAME).forEach(node ->
                categories.add(new Category(node.path("id").asText(), node.path("name").asText())));
        return categories;
    }

    @Override
    public Optional<Category> findById(String id) {
        for (JsonNode node : store.read(FILE_NAME)) {
            if (id.equals(node.path("id").asText())) {
                return Optional.of(new Category(node.path("id").asText(), node.path("name").asText()));
            }
        }
        return Optional.empty();
    }
}
