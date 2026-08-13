package edu.udistrital.sig.application.service;

import edu.udistrital.sig.application.support.InMemoryEntityRepository;
import edu.udistrital.sig.domain.exception.EntityNotFoundException;
import edu.udistrital.sig.domain.exception.InvalidEntityException;
import edu.udistrital.sig.domain.model.Category;
import edu.udistrital.sig.domain.model.Coordinate;
import edu.udistrital.sig.domain.model.EntityNature;
import edu.udistrital.sig.domain.model.GeographicEntity;
import edu.udistrital.sig.domain.model.LineString;
import edu.udistrital.sig.domain.model.Point;
import edu.udistrital.sig.domain.model.Polygon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryServiceTest {

    private static final Category TURISMO = new Category("TURISMO", "Turismo");
    private static final Category VIA = new Category("VIA", "Vía");
    private static final Category BARRIO = new Category("BARRIO", "Barrio");

    private QueryService service;

    @BeforeEach
    void setUp() {
        InMemoryEntityRepository repository = new InMemoryEntityRepository();
        repository.save(GeographicEntity.create(
                "torre", "Torre Colpatria", "Punto de referencia",
                EntityNature.POINT_OF_INTEREST, TURISMO, Map.of("altura", "196 m"),
                new Point(new Coordinate(4.612, -74.071))));
        repository.save(GeographicEntity.create(
                "av-eldorado", "Avenida El Dorado", "Conexión con el aeropuerto",
                EntityNature.ROAD, VIA, Map.of(),
                new LineString(List.of(
                        new Coordinate(4.68, -74.15),
                        new Coordinate(4.66, -74.12)))));
        repository.save(GeographicEntity.create(
                "candelaria", "La Candelaria", "Sector histórico",
                EntityNature.NEIGHBORHOOD, BARRIO, Map.of(),
                new Polygon(List.of(
                        new Coordinate(4.595, -74.078),
                        new Coordinate(4.601, -74.078),
                        new Coordinate(4.601, -74.070),
                        new Coordinate(4.595, -74.070),
                        new Coordinate(4.595, -74.078)))));
        service = new QueryService(repository);
    }

    @Test
    void filtersByCategoryId() {
        QueryService.QueryResult result = service.query("TURISMO", null, null, null, null, null);

        assertEquals(1, result.entities().size());
        assertEquals("torre", result.entities().get(0).id());
        assertEquals("CATEGORY", result.matchedBy());
    }

    @Test
    void filtersByCategoryName() {
        QueryService.QueryResult result = service.query("Vía", null, null, null, null, null);

        assertEquals(1, result.entities().size());
        assertEquals("av-eldorado", result.entities().get(0).id());
    }

    @Test
    void filtersByTextOnNameOrDescription() {
        QueryService.QueryResult result = service.query(null, null, "aeropuerto", null, null, null);

        assertEquals(1, result.entities().size());
        assertEquals("av-eldorado", result.entities().get(0).id());
        assertEquals("TEXT", result.matchedBy());
    }

    @Test
    void filtersByAttributeValue() {
        QueryService.QueryResult result = service.query(null, "196", null, null, null, null);

        assertEquals(1, result.entities().size());
        assertEquals("torre", result.entities().get(0).id());
        assertEquals("ATTRIBUTE", result.matchedBy());
    }

    @Test
    void proximityMatchesPointInsideRadius() {
        QueryService.QueryResult result = service.query(null, null, null, 4.612, -74.071, 1.0);

        assertTrue(result.entities().stream().anyMatch(e -> e.id().equals("torre")));
        assertEquals("PROXIMITY", result.matchedBy());
    }

    @Test
    void proximityMatchesLineWithinRadius() {
        QueryService.QueryResult result = service.query(null, null, null, 4.67, -74.13, 1.0);

        assertTrue(result.entities().stream().anyMatch(e -> e.id().equals("av-eldorado")));
    }

    @Test
    void proximityMatchesPolygonContainment() {
        QueryService.QueryResult result = service.query(null, null, null, 4.598, -74.074, 0.5);

        assertTrue(result.entities().stream().anyMatch(e -> e.id().equals("candelaria")));
    }

    @Test
    void proximityExcludesEntitiesBeyondRadius() {
        QueryService.QueryResult result = service.query(null, null, null, 4.598, -74.074, 0.01);

        assertTrue(result.entities().stream().noneMatch(e -> e.id().equals("torre")));
    }

    @Test
    void noMatchesReturnsEmptyListWithoutError() {
        QueryService.QueryResult result = service.query(null, null, "texto que no existe", null, null, null);

        assertEquals(0, result.entities().size());
    }

    @Test
    void combinesCategoryAndTextWithAndSemantics() {
        QueryService.QueryResult result = service.query("TURISMO", null, "torre", null, null, null);

        assertEquals(1, result.entities().size());
        assertEquals("torre", result.entities().get(0).id());
        assertEquals("CATEGORY,TEXT", result.matchedBy());
    }

    @Test
    void combinedCriteriaWithNoIntersectionReturnEmpty() {
        QueryService.QueryResult result = service.query("TURISMO", null, "aeropuerto", null, null, null);

        assertEquals(0, result.entities().size());
        assertEquals("CATEGORY,TEXT", result.matchedBy());
    }

    @Test
    void combinesAttributeAndText() {
        QueryService.QueryResult result = service.query(null, "196", "torre", null, null, null);

        assertEquals(1, result.entities().size());
        assertEquals("torre", result.entities().get(0).id());
        assertEquals("ATTRIBUTE,TEXT", result.matchedBy());
    }

    @Test
    void combinesCategoryAndProximity() {
        QueryService.QueryResult result = service.query("TURISMO", null, null, 4.612, -74.071, 1.0);

        assertEquals(1, result.entities().size());
        assertEquals("torre", result.entities().get(0).id());
        assertEquals("CATEGORY,PROXIMITY", result.matchedBy());
    }

    @Test
    void combinedProximityExcludesEntitiesOfOtherCategory() {
        QueryService.QueryResult result = service.query("VIA", null, null, 4.612, -74.071, 1.0);

        assertEquals(0, result.entities().size());
    }

    @Test
    void combinesAllCriteriaTogether() {
        QueryService.QueryResult result = service.query("TURISMO", "196", "torre", 4.612, -74.071, 1.0);

        assertEquals(1, result.entities().size());
        assertEquals("torre", result.entities().get(0).id());
        assertEquals("CATEGORY,ATTRIBUTE,TEXT,PROXIMITY", result.matchedBy());
    }

    @Test
    void queryWithoutAnyCriterionIsRejected() {
        assertThrows(InvalidEntityException.class, () -> service.query(null, null, null, null, null, null));
    }

    @Test
    void incompleteProximityIsRejected() {
        assertThrows(InvalidEntityException.class, () -> service.query(null, null, null, 4.6, null, 1.0));
    }

    @Test
    void nonPositiveRadiusIsRejected() {
        assertThrows(InvalidEntityException.class, () -> service.query(null, null, null, 4.6, -74.0, 0.0));
    }

    @Test
    void outOfRangeCoordinateIsRejected() {
        assertThrows(edu.udistrital.sig.domain.exception.InvalidGeometryException.class,
                () -> service.query(null, null, null, 200.0, -74.0, 1.0));
    }
}
