package edu.udistrital.sig.infrastructure.persistence;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import edu.udistrital.sig.domain.model.Category;
import edu.udistrital.sig.domain.model.Coordinate;
import edu.udistrital.sig.domain.model.EntityNature;
import edu.udistrital.sig.domain.model.GeographicEntity;
import edu.udistrital.sig.domain.model.LineString;
import edu.udistrital.sig.domain.model.Point;
import edu.udistrital.sig.domain.model.Polygon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityRepositoryJsonTest {

    @TempDir
    Path tempDir;

    private EntityRepositoryJson repository;

    @BeforeEach
    void setUp() {
        JsonDataStore store = new JsonDataStore(tempDir.toString());
        store.write("categories.json", JsonNodeFactory.instance.arrayNode()
                .add(JsonNodeFactory.instance.objectNode().put("id", "TURISMO").put("name", "Turismo"))
                .add(JsonNodeFactory.instance.objectNode().put("id", "VIA").put("name", "Vía"))
                .add(JsonNodeFactory.instance.objectNode().put("id", "BARRIO").put("name", "Barrio")));
        repository = new EntityRepositoryJson(store, new CategoryRepositoryJson(store));
    }

    private GeographicEntity point(String id, String name) {
        return GeographicEntity.create(id, name, "Descripción",
                EntityNature.POINT_OF_INTEREST, new Category("TURISMO", "Turismo"), Map.of("altura", "196 m"),
                new Point(new Coordinate(4.612, -74.071)));
    }

    @Test
    void savePersistsAndFindByIdRetrievesAllFields() {
        repository.save(point("id-1", "Torre Colpatria"));

        Optional<GeographicEntity> found = repository.findById("id-1");
        assertTrue(found.isPresent());
        assertEquals("Torre Colpatria", found.get().name());
        assertEquals("Descripción", found.get().description());
        assertEquals("196 m", found.get().attributes().get("altura"));
        assertEquals("TURISMO", found.get().category().id());
        assertEquals("Point", found.get().geometry().type());
    }

    @Test
    void saveWithSameIdReplacesEntity() {
        repository.save(point("id-1", "Antes"));
        repository.save(point("id-1", "Después"));

        assertEquals(1, repository.findAll().size());
        assertEquals("Después", repository.findById("id-1").orElseThrow().name());
    }

    @Test
    void savePersistsLineAndPolygonGeometries() {
        repository.save(GeographicEntity.create("line-1", "Vía", null,
                EntityNature.ROAD, new Category("VIA", "Vía"), Map.of(),
                new LineString(List.of(new Coordinate(4.68, -74.15), new Coordinate(4.66, -74.12)))));
        repository.save(GeographicEntity.create("poly-1", "Barrio", null,
                EntityNature.NEIGHBORHOOD, new Category("BARRIO", "Barrio"), Map.of(),
                new Polygon(List.of(
                        new Coordinate(4.595, -74.078),
                        new Coordinate(4.601, -74.078),
                        new Coordinate(4.601, -74.070),
                        new Coordinate(4.595, -74.070),
                        new Coordinate(4.595, -74.078)))));

        assertEquals("LineString", repository.findById("line-1").orElseThrow().geometry().type());
        assertEquals(2, ((LineString) repository.findById("line-1").orElseThrow().geometry()).coordinates().size());
        assertEquals(5, ((Polygon) repository.findById("poly-1").orElseThrow().geometry()).ring().size());
    }

    @Test
    void deleteRemovesOnlyMatchingEntity() {
        repository.save(point("id-1", "Uno"));
        repository.save(point("id-2", "Dos"));

        repository.delete("id-1");

        assertEquals(1, repository.findAll().size());
        assertTrue(repository.findById("id-2").isPresent());
        assertTrue(repository.findById("id-1").isEmpty());
    }
}
