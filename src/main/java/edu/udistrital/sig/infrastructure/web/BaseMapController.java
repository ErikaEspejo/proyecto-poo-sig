package edu.udistrital.sig.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/map")
public class BaseMapController {

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    public BaseMapController(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/basemap")
    public ResponseEntity<JsonNode> basemap() {
        Resource resource = resourceLoader.getResource("classpath:data/colombia-boundaries.geojson");
        try {
            JsonNode node = objectMapper.readTree(resource.getInputStream());
            return ResponseEntity.ok(node);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
