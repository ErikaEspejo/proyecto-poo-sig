package edu.udistrital.sig.domain.model;

import edu.udistrital.sig.domain.exception.InvalidGeometryException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeometryTest {

    @Test
    void pointRejectsLatitudeOutOfRange() {
        assertThrows(InvalidGeometryException.class, () -> new Coordinate(100.0, -74.0));
    }

    @Test
    void pointRejectsLongitudeOutOfRange() {
        assertThrows(InvalidGeometryException.class, () -> new Coordinate(4.6, 200.0));
    }

    @Test
    void pointMinDistanceIsHaversineToCoordinate() {
        Point point = new Point(new Coordinate(4.611, -74.072));
        double distance = point.minDistanceToKm(new Coordinate(6.2518, -75.5636));

        assertEquals(246.07, distance, 1.0);
    }

    @Test
    void lineStringRequiresAtLeastTwoCoordinates() {
        assertThrows(InvalidGeometryException.class,
                () -> new LineString(List.of(new Coordinate(4.6, -74.07))));
    }

    @Test
    void lineStringMinDistanceUsesNearestSegment() {
        LineString line = new LineString(List.of(
                new Coordinate(4.0, -74.0),
                new Coordinate(4.0, -73.0)));
        Coordinate reference = new Coordinate(4.5, -73.5);

        assertEquals(55.287, line.minDistanceToKm(reference), 0.5);
    }

    @Test
    void polygonRequiresClosedRing() {
        List<Coordinate> openRing = List.of(
                new Coordinate(4.0, -74.0),
                new Coordinate(5.0, -74.0),
                new Coordinate(5.0, -73.0),
                new Coordinate(4.0, -73.0));

        assertThrows(InvalidGeometryException.class, () -> new Polygon(openRing));
    }

    @Test
    void polygonRequiresMinimumCoordinates() {
        List<Coordinate> tooSmall = List.of(
                new Coordinate(4.0, -74.0),
                new Coordinate(4.0, -74.0),
                new Coordinate(4.0, -74.0),
                new Coordinate(4.0, -74.0));

        assertThrows(InvalidGeometryException.class, () -> new Polygon(tooSmall));
    }

    @Test
    void polygonMinDistanceIsZeroWhenReferenceIsInside() {
        Polygon polygon = new Polygon(List.of(
                new Coordinate(4.0, -74.0),
                new Coordinate(5.0, -74.0),
                new Coordinate(5.0, -73.0),
                new Coordinate(4.0, -73.0),
                new Coordinate(4.0, -74.0)));

        assertEquals(0.0, polygon.minDistanceToKm(new Coordinate(4.5, -73.5)), 1e-9);
    }

    @Test
    void polygonMinDistanceToOutsidePointUsesRing() {
        Polygon polygon = new Polygon(List.of(
                new Coordinate(4.0, -74.0),
                new Coordinate(5.0, -74.0),
                new Coordinate(5.0, -73.0),
                new Coordinate(4.0, -73.0),
                new Coordinate(4.0, -74.0)));

        double distance = polygon.minDistanceToKm(new Coordinate(3.0, -73.5));

        assertTrue(distance > 100.0);
    }
}
