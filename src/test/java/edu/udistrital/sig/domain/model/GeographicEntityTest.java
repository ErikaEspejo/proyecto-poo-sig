package edu.udistrital.sig.domain.model;

import edu.udistrital.sig.domain.exception.InvalidEntityException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeographicEntityTest {

    private static final Category TURISMO = new Category("TURISMO", "Turismo");

    @Test
    void validEntityIsCreated() {
        GeographicEntity entity = GeographicEntity.create(
                "id-1", "Torre Colpatria", "Punto de referencia",
                EntityNature.POINT_OF_INTEREST, TURISMO, Map.of("altura", "196 m"),
                new Point(new Coordinate(4.612, -74.071)));

        assertEquals("id-1", entity.id());
        assertEquals("Torre Colpatria", entity.name());
        assertEquals("196 m", entity.attributes().get("altura"));
    }

    @Test
    void entityRequiresName() {
        assertThrows(InvalidEntityException.class, () -> GeographicEntity.create(
                "id-1", "", "Punto de referencia",
                EntityNature.POINT_OF_INTEREST, TURISMO, Map.of(),
                new Point(new Coordinate(4.612, -74.071))));
    }

    @Test
    void entityRequiresCategory() {
        assertThrows(InvalidEntityException.class, () -> GeographicEntity.create(
                "id-1", "Torre Colpatria", "Punto de referencia",
                EntityNature.POINT_OF_INTEREST, null, Map.of(),
                new Point(new Coordinate(4.612, -74.071))));
    }

    @Test
    void entityRequiresGeometry() {
        assertThrows(InvalidEntityException.class, () -> GeographicEntity.create(
                "id-1", "Torre Colpatria", "Punto de referencia",
                EntityNature.POINT_OF_INTEREST, TURISMO, Map.of(), null));
    }

    @Test
    void entityRequiresNature() {
        assertThrows(InvalidEntityException.class, () -> GeographicEntity.create(
                "id-1", "Torre Colpatria", "Punto de referencia",
                null, TURISMO, Map.of(),
                new Point(new Coordinate(4.612, -74.071))));
    }

    @Test
    void updateKeepsIdentifier() {
        GeographicEntity entity = GeographicEntity.create(
                "id-1", "Torre Colpatria", "Punto de referencia",
                EntityNature.POINT_OF_INTEREST, TURISMO, Map.of(),
                new Point(new Coordinate(4.612, -74.071)));

        GeographicEntity updated = entity.updatedWith(
                "Nuevo nombre", null,
                EntityNature.POINT_OF_INTEREST, TURISMO, Map.of(),
                new Point(new Coordinate(4.6, -74.1)));

        assertEquals("id-1", updated.id());
        assertEquals("Nuevo nombre", updated.name());
    }

    @Test
    void attributesAreImmutableCopies() {
        Map<String, String> mutable = new java.util.HashMap<>(Map.of("altura", "196 m"));
        GeographicEntity entity = GeographicEntity.create(
                "id-1", "Torre Colpatria", null,
                EntityNature.POINT_OF_INTEREST, TURISMO, mutable, new Point(new Coordinate(4.612, -74.071)));
        mutable.put("extra", "nuevo");

        assertEquals(Map.of("altura", "196 m"), entity.attributes());
    }
}
