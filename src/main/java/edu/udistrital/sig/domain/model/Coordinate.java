package edu.udistrital.sig.domain.model;

import edu.udistrital.sig.domain.exception.InvalidGeometryException;

public record Coordinate(double latitude, double longitude) {

    public Coordinate {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new InvalidGeometryException("La latitud debe estar entre -90 y 90 grados.");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new InvalidGeometryException("La longitud debe estar entre -180 y 180 grados.");
        }
    }

    public static Coordinate ofLonLat(double longitude, double latitude) {
        return new Coordinate(latitude, longitude);
    }
}
