package edu.udistrital.sig.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "sig.data.directory=target/test-data")
@AutoConfigureMockMvc
class WebApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String consultaToken;

    @BeforeEach
    void login() throws Exception {
        adminToken = login("admin", "admin123");
        consultaToken = login("consulta", "consulta123");
    }

    private String login(String username, String password) throws Exception {
        ObjectNode body = objectMapper.createObjectNode()
                .put("username", username)
                .put("password", password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private ObjectNode pointBody(String name, String category, double lat, double lon) {
        ObjectNode attributes = objectMapper.createObjectNode();
        attributes.put("origen", "test");
        ObjectNode geometry = objectMapper.createObjectNode();
        geometry.put("type", "Point");
        ArrayNode coordinates = objectMapper.createArrayNode();
        coordinates.add(lon);
        coordinates.add(lat);
        geometry.set("coordinates", coordinates);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", name);
        body.put("description", "Creada en la prueba");
        body.put("nature", "POINT_OF_INTEREST");
        body.put("category", category);
        body.set("attributes", attributes);
        body.set("geometry", geometry);
        return body;
    }

    @Test
    void wrongPasswordIsRejectedWithSpanishMessage() throws Exception {
        ObjectNode body = objectMapper.createObjectNode().put("username", "admin").put("password", "mala");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas."));
    }

    @Test
    void entitiesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/entities"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Debe iniciar sesión."));
    }

    @Test
    void listingEntitiesRequiresToken() throws Exception {
        mockMvc.perform(get("/api/entities").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entities").isArray());
    }

    @Test
    void adminCanCreateEntity() throws Exception {
        String name = "Entidad de prueba " + System.nanoTime();
        ObjectNode body = pointBody(name, "TURISMO", 4.612, -74.071);

        MvcResult result = mockMvc.perform(post("/api/entities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
        mockMvc.perform(get("/api/entities/" + created.get("id").asText())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));

        mockMvc.perform(delete("/api/entities/" + created.get("id").asText())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void invalidLatitudeIsRejectedWithSpanishMessage() throws Exception {
        ObjectNode body = pointBody("Inválida", "TURISMO", 100.0, -74.071);
        mockMvc.perform(post("/api/entities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("latitud")));
    }

    @Test
    void unclosedPolygonIsRejected() throws Exception {
        ArrayNode ring = objectMapper.createArrayNode();
        ring.add(objectMapper.createArrayNode().add(-74.078).add(4.595));
        ring.add(objectMapper.createArrayNode().add(-74.070).add(4.595));
        ring.add(objectMapper.createArrayNode().add(-74.070).add(4.601));
        ring.add(objectMapper.createArrayNode().add(-74.078).add(4.601));
        ObjectNode geometry = objectMapper.createObjectNode();
        geometry.put("type", "Polygon");
        ArrayNode rings = objectMapper.createArrayNode();
        rings.add(ring);
        geometry.set("coordinates", rings);
        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", "Polígono abierto");
        body.put("nature", "NEIGHBORHOOD");
        body.put("category", "BARRIO");
        body.set("attributes", objectMapper.createObjectNode());
        body.set("geometry", geometry);

        mockMvc.perform(post("/api/entities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("cerrado")));
    }

    @Test
    void missingCategoryIsRejected() throws Exception {
        ObjectNode geometry = objectMapper.createObjectNode();
        geometry.put("type", "Point");
        ArrayNode coordinates = objectMapper.createArrayNode();
        coordinates.add(-74.071);
        coordinates.add(4.612);
        geometry.set("coordinates", coordinates);
        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", "Sin categoría");
        body.put("nature", "POINT_OF_INTEREST");
        body.set("attributes", objectMapper.createObjectNode());
        body.set("geometry", geometry);

        mockMvc.perform(post("/api/entities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("categoría")));
    }

    @Test
    void consultationUserCannotCreateEntity() throws Exception {
        ObjectNode body = pointBody("Prohibida para consulta", "TURISMO", 4.612, -74.071);
        mockMvc.perform(post("/api/entities")
                        .header("Authorization", "Bearer " + consultaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void consultationUserCannotDeleteEntity() throws Exception {
        mockMvc.perform(delete("/api/entities/no-existe")
                        .header("Authorization", "Bearer " + consultaToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOfNonexistentEntityIsNotFound() throws Exception {
        ObjectNode body = pointBody("Actualización", "TURISMO", 4.612, -74.071);
        mockMvc.perform(put("/api/entities/no-existe")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOfNonexistentEntityIsNotFound() throws Exception {
        mockMvc.perform(get("/api/entities/no-existe")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void queryByCategoryReturnsOnlyMatches() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/entities/query")
                        .param("category", "TURISMO")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        for (JsonNode entity : body.get("entities")) {
            assertEquals("TURISMO", entity.get("category").asText());
        }
        assertTrue(body.get("entities").size() >= 1);
    }

    @Test
    void queryByProximityReturnsMatchesAndMatchedBy() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/entities/query")
                        .param("lat", "4.612")
                        .param("lon", "-74.071")
                        .param("radiusKm", "5")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("PROXIMITY", body.get("matchedBy").asText());
        assertTrue(body.get("entities").size() >= 1);
    }

    @Test
    void queryWithoutCriteriaIsRejected() throws Exception {
        mockMvc.perform(get("/api/entities/query")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void queryWithNoMatchesReturnsEmptyListNotError() throws Exception {
        mockMvc.perform(get("/api/entities/query")
                        .param("text", "texto-inexistente-xyz")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entities").isEmpty());
    }

    @Test
    void categoriesEndpointReturnsPredefinedCategories() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories[0].id").exists());
    }

    @Test
    void basemapIsAvailableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/map/basemap"))
                .andExpect(status().isOk());
    }

    @Test
    void missingStaticResourceIsNotFoundNotServerError() throws Exception {
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso no encontrado."));
    }
}
