package edu.udistrital.sig.domain.model;

import edu.udistrital.sig.domain.exception.InvalidGeometryException;

import java.util.List;

public final class Polygon extends Geometry {

    private final List<Coordinate> ring;

    public Polygon(List<Coordinate> ring) {
        if (ring == null || ring.size() < 4) {
            throw new InvalidGeometryException("Un polígono requiere al menos cuatro coordenadas.");
        }
        Coordinate first = ring.get(0);
        Coordinate last = ring.get(ring.size() - 1);
        if (!first.equals(last)) {
            throw new InvalidGeometryException("El anillo del polígono debe estar cerrado.");
        }
        if (ring.subList(0, ring.size() - 1).stream().distinct().count() < 3) {
            throw new InvalidGeometryException("El polígono debe tener al menos tres puntos distintos.");
        }
        this.ring = List.copyOf(ring);
    }

    public List<Coordinate> ring() {
        return ring;
    }

    @Override
    public String type() {
        return "Polygon";
    }

    @Override
    public double minDistanceToKm(Coordinate reference) {
        if (GeoMath.pointInRing(reference, ring)) {
            return 0.0;
        }
        double min = Double.MAX_VALUE;
        for (int i = 0; i < ring.size() - 1; i++) {
            min = Math.min(min, GeoMath.pointToSegmentDistanceKm(
                    reference, ring.get(i), ring.get(i + 1)));
        }
        return min;
    }
}
