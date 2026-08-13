package edu.udistrital.sig.infrastructure.web;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.udistrital.sig.application.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ObjectNode list() {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        categoryService.findAll().forEach(category -> array.add(
                JsonNodeFactory.instance.objectNode()
                        .put("id", category.id())
                        .put("name", category.name())));
        return JsonNodeFactory.instance.objectNode().set("categories", array);
    }
}
