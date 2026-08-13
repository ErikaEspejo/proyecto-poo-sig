package edu.udistrital.sig.domain.model;

import edu.udistrital.sig.domain.exception.InvalidGeometryException;

import java.util.List;

public final class LineString extends Geometry {

    private final List<Coordinate> coordinates;

    public LineString(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.size() < 2) {
            throw new InvalidGeometryException("Una línea requiere al menos dos coordenadas.");
        }
        this.coordinates = List.copyOf(coordinates);
    }

    public List<Coordinate> coordinates() {
        return coordinates;
    }

    @Override
    public String type() {
        return "LineString";
    }

    @Override
    public double minDistanceToKm(Coordinate reference) {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            min = Math.min(min, GeoMath.pointToSegmentDistanceKm(
                    reference, coordinates.get(i), coordinates.get(i + 1)));
        }
        return min;
    }
}
