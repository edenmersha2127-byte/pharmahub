package com.medicinelocator.inventory.application.dto.response;

import java.util.UUID;

public class PharmacyProfileResponse {

    private UUID pharmacyId;
    private String pharmacyName;
    private String email;
    private String address;
    private String city;
    private double latitude;
    private double longitude;
    private String licenseNumber;
    private String phoneNumber;
    private boolean open;

    public PharmacyProfileResponse() {
    }

    public PharmacyProfileResponse(UUID pharmacyId, String pharmacyName, String email,
                                   String address, String city, double latitude,
                                   double longitude, String licenseNumber, String phoneNumber) {
        this.pharmacyId = pharmacyId;
        this.pharmacyName = pharmacyName;
        this.email = email;
        this.address = address;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.licenseNumber = licenseNumber;
        this.phoneNumber = phoneNumber;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public String getPharmacyName() { return pharmacyName; }
    public void setPharmacyName(String pharmacyName) { this.pharmacyName = pharmacyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }
}