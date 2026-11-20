package com.medicinelocator.salesservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A pharmacy's own walk-in customer (CRM contact). Distinct from the
 * marketplace login customer in auth — belongs to exactly one pharmacy and
 * exists only for sales history and quick lookup at the POS.
 */
public class PharmacyCustomer {

    private UUID id;
    private UUID pharmacyId;
    private String name;
    private String phone;
    private String email;
    private String note;
    private LocalDateTime createdAt;

    public PharmacyCustomer() {
    }

    public PharmacyCustomer(UUID id, UUID pharmacyId, String name, String phone,
                            String email, String note, LocalDateTime createdAt) {
        this.id = id;
        this.pharmacyId = pharmacyId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.note = note;
        this.createdAt = createdAt;
    }

    public void updateDetails(String name, String phone, String email, String note) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.note = note;
    }

    public boolean belongsTo(UUID pharmacyId) {
        return this.pharmacyId.equals(pharmacyId);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
