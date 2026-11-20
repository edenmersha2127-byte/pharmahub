package com.medicinelocator.search.application.query;


public class SearchNearbyPharmaciesQuery {

    private final double latitude;
    private final double longitude;
    private final double radiusKm;
    private final int page;
    private final int size;

    public SearchNearbyPharmaciesQuery(double latitude, double longitude,
                                       double radiusKm, int page, int size) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusKm = radiusKm;
        this.page = page;
        this.size = size;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getRadiusKm() { return radiusKm; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}