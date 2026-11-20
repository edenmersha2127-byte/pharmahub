package com.medicinelocator.search.application.service;


public interface GeoLocationService {

    double toMetres(double radiusKm);

    boolean isValidCoordinate(double latitude, double longitude);

    String toPostGisPoint(double longitude, double latitude);
}