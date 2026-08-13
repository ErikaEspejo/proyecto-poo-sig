package edu.udistrital.sig.domain.model;

import edu.udistrital.sig.domain.exception.InvalidGeometryException;

public final class Point extends Geometry {

    private final Coordinate coordinate;

    public Point(Coordinate coordinate) {
        if (coordinate == null) {
            throw new InvalidGeometryException("Un punto requiere una coordenada.");
        }
        this.coordinate = coordinate;
    }

    public Coordinate coordinate() {
        return coordinate;
    }

    @Override
    public String type() {
        return "Point";
    }

    @Override
    public double minDistanceToKm(Coordinate reference) {
        return GeoMath.haversineKm(reference, coordinate);
    }
}
