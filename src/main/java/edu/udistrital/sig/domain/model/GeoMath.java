package edu.udistrital.sig.domain.model;

import java.util.List;

public final class GeoMath {

    public static final double EARTH_RADIUS_KM = 6371.0;
    public static final double KM_PER_LATITUDE_DEGREE = 110.574;
    public static final double KM_PER_LONGITUDE_DEGREE = 111.320;

    private GeoMath() {
    }

    public static double haversineKm(Coordinate a, Coordinate b) {
        double lat1 = Math.toRadians(a.latitude());
        double lat2 = Math.toRadians(b.latitude());
        double deltaLat = Math.toRadians(b.latitude() - a.latitude());
        double deltaLon = Math.toRadians(b.longitude() - a.longitude());

        double h = Math.pow(Math.sin(deltaLat / 2.0), 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLon / 2.0), 2);
        return 2.0 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(h));
    }

    public static double pointToSegmentDistanceKm(Coordinate p, Coordinate a, Coordinate b) {
        double referenceLatitude = (a.latitude() + b.latitude()) / 2.0;

        double px = planimetricLonKm(p.longitude(), referenceLatitude);
        double py = planimetricLatKm(p.latitude());
        double ax = planimetricLonKm(a.longitude(), referenceLatitude);
        double ay = planimetricLatKm(a.latitude());
        double bx = planimetricLonKm(b.longitude(), referenceLatitude);
        double by = planimetricLatKm(b.latitude());

        double dx = bx - ax;
        double dy = by - ay;
        double lengthSquared = dx * dx + dy * dy;

        double t = lengthSquared == 0.0
                ? 0.0
                : Math.max(0.0, Math.min(1.0, ((px - ax) * dx + (py - ay) * dy) / lengthSquared));

        double closestX = ax + t * dx;
        double closestY = ay + t * dy;

        return Math.hypot(px - closestX, py - closestY);
    }

    public static boolean pointInRing(Coordinate p, List<Coordinate> ring) {
        boolean inside = false;
        for (int i = 0, j = ring.size() - 1; i < ring.size(); j = i++) {
            Coordinate a = ring.get(i);
            Coordinate b = ring.get(j);

            boolean crossesRay = (a.latitude() > p.latitude()) != (b.latitude() > p.latitude())
                    && p.longitude() < (b.longitude() - a.longitude())
                    * (p.latitude() - a.latitude()) / (b.latitude() - a.latitude())
                    + a.longitude();
            if (crossesRay) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static double planimetricLatKm(double latitude) {
        return latitude * KM_PER_LATITUDE_DEGREE;
    }

    private static double planimetricLonKm(double longitude, double referenceLatitude) {
        return longitude * KM_PER_LONGITUDE_DEGREE * Math.cos(Math.toRadians(referenceLatitude));
    }
}
