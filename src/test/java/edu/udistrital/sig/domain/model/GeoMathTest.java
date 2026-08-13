package edu.udistrital.sig.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoMathTest {

    @Test
    void haversineMatchesKnownDistanceBetweenBogotaAndMedellin() {
        Coordinate bogota = new Coordinate(4.611, -74.072);
        Coordinate medellin = new Coordinate(6.2518, -75.5636);

        double distance = GeoMath.haversineKm(bogota, medellin);

        assertEquals(246.07, distance, 1.0);
    }

    @Test
    void haversineIsZeroForIdenticalPoints() {
        Coordinate point = new Coordinate(4.611, -74.072);
        assertEquals(0.0, GeoMath.haversineKm(point, point), 1e-9);
    }

    @Test
    void pointToSegmentDistanceIsLatitudeDifferenceForPerpendicularPoint() {
        Coordinate segmentStart = new Coordinate(4.0, -74.0);
        Coordinate segmentEnd = new Coordinate(4.0, -73.0);
        Coordinate reference = new Coordinate(4.5, -73.5);

        double distance = GeoMath.pointToSegmentDistanceKm(reference, segmentStart, segmentEnd);

        assertEquals(55.287, distance, 0.5);
    }

    @Test
    void pointOnSegmentHasNearZeroDistance() {
        Coordinate segmentStart = new Coordinate(4.0, -74.0);
        Coordinate segmentEnd = new Coordinate(4.0, -73.0);
        Coordinate onSegment = new Coordinate(4.0, -73.5);

        double distance = GeoMath.pointToSegmentDistanceKm(onSegment, segmentStart, segmentEnd);

        assertTrue(distance < 0.01);
    }

    @Test
    void pointBeyondSegmentClampsToNearestEndpoint() {
        Coordinate segmentStart = new Coordinate(4.0, -74.0);
        Coordinate segmentEnd = new Coordinate(4.0, -73.0);
        Coordinate beyond = new Coordinate(4.2, -74.0);

        double distance = GeoMath.pointToSegmentDistanceKm(beyond, segmentStart, segmentEnd);

        assertEquals(0.2 * GeoMath.KM_PER_LATITUDE_DEGREE, distance, 0.5);
    }

    @Test
    void pointInsideRingIsDetected() {
        List<Coordinate> ring = List.of(
                new Coordinate(4.0, -74.0),
                new Coordinate(5.0, -74.0),
                new Coordinate(5.0, -73.0),
                new Coordinate(4.0, -73.0),
                new Coordinate(4.0, -74.0));

        assertTrue(GeoMath.pointInRing(new Coordinate(4.5, -73.5), ring));
    }

    @Test
    void pointOutsideRingIsRejected() {
        List<Coordinate> ring = List.of(
                new Coordinate(4.0, -74.0),
                new Coordinate(5.0, -74.0),
                new Coordinate(5.0, -73.0),
                new Coordinate(4.0, -73.0),
                new Coordinate(4.0, -74.0));

        assertFalse(GeoMath.pointInRing(new Coordinate(3.0, -73.0), ring));
    }
}
