package com.medicinelocator.prescription.domain.model;

import java.util.UUID;

/**
 * A hospital this pharmacy platform is integrated with. Populated by an admin
 * during onboarding; used to route a verification call to the right hospital.
 */
public class HospitalConnection {

    private UUID id;
    private String name;
    private String email;
    private String hospitalId;
    private String baseUrl;
    private String apiKey;
    private boolean active;

    public HospitalConnection() {
    }

    public HospitalConnection(UUID id, String name, String email, String hospitalId,
                              String baseUrl, String apiKey, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.hospitalId = hospitalId;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.active = active;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
