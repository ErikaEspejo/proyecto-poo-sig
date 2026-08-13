package edu.udistrital.sig.domain.model;

public abstract class Geometry {

    public abstract String type();

    public abstract double minDistanceToKm(Coordinate coordinate);
}
