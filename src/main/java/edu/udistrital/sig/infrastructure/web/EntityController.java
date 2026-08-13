package edu.udistrital.sig.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.udistrital.sig.application.service.EntityService;
import edu.udistrital.sig.application.service.QueryService;
import edu.udistrital.sig.domain.exception.EntityNotFoundException;
import edu.udistrital.sig.domain.model.GeographicEntity;
import edu.udistrital.sig.domain.model.Role;
import edu.udistrital.sig.domain.model.User;
import edu.udistrital.sig.domain.repository.CategoryRepository;
import edu.udistrital.sig.infrastructure.codec.EntityJsonCodec;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/entities")
public class EntityController {

    private final EntityService entityService;
    private final QueryService queryService;
    private final CategoryRepository categoryRepository;

    public EntityController(EntityService entityService, QueryService queryService,
                            CategoryRepository categoryRepository) {
        this.entityService = entityService;
        this.queryService = queryService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ObjectNode list() {
        return entitiesResponse(entityService.findAll());
    }

    @GetMapping("/{id}")
    public ObjectNode get(@PathVariable String id) {
        return EntityJsonCodec.toJson(entityService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id)));
    }

    @PostMapping
    public ResponseEntity<ObjectNode> create(@RequestBody JsonNode body, HttpServletRequest request) {
        String id = UUID.randomUUID().toString();
        GeographicEntity entity = EntityRequestMapper.toDomain(body, id, categoryRepository);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EntityJsonCodec.toJson(entityService.create(entity, currentRole(request))));
    }

    @PutMapping("/{id}")
    public ObjectNode update(@PathVariable String id, @RequestBody JsonNode body, HttpServletRequest request) {
        GeographicEntity draft = EntityRequestMapper.toDomain(body, id, categoryRepository);
        return EntityJsonCodec.toJson(entityService.update(id, draft, currentRole(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, HttpServletRequest request) {
        entityService.delete(id, currentRole(request));
        return ResponseEntity.noContent().build();
    }

    private Role currentRole(HttpServletRequest request) {
        return ((User) request.getAttribute("currentUser")).role();
    }

    @GetMapping("/query")
    public ObjectNode query(@RequestParam(required = false) String category,
                            @RequestParam(required = false) String attribute,
                            @RequestParam(required = false) String text,
                            @RequestParam(required = false) Double lat,
                            @RequestParam(required = false) Double lon,
                            @RequestParam(required = false) Double radiusKm) {
        QueryService.QueryResult result = queryService.query(category, attribute, text, lat, lon, radiusKm);
        ObjectNode response = entitiesResponse(result.entities());
        response.put("matchedBy", result.matchedBy());
        return response;
    }

    private ObjectNode entitiesResponse(List<GeographicEntity> entities) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        entities.forEach(entity -> array.add(EntityJsonCodec.toJson(entity)));
        return JsonNodeFactory.instance.objectNode().set("entities", array);
    }
}
