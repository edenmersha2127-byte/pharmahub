package com.medicinelocator.auth.domain.model;

import com.medicinelocator.auth.domain.enums.AccountStatus;
import com.medicinelocator.auth.domain.enums.PharmacyStatus;
import com.medicinelocator.auth.domain.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public class Pharmacy {

    private UUID id;
    private String email;
    private String passwordHash;
    private String pharmacyName;
    private String licenseNumber;
    private String phoneNumber;
    private String address;
    private String city;
    private double latitude;
    private double longitude;
    private AccountStatus accountStatus;
    private PharmacyStatus pharmacyStatus;
    private boolean emailVerified;
    private boolean isOpen = false;
    private int failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private int strikeCount;
    private LocalDateTime suspendedUntil;
    private String subscriptionStatus;      // NONE | TRIAL | ACTIVE | EXPIRED (owned by payment-service)
    private LocalDateTime subscribedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Pharmacy() {
    }

    public Pharmacy(UUID id, String email, String passwordHash, String pharmacyName,
                    String licenseNumber, String phoneNumber, String address, String city,
                    double latitude, double longitude, AccountStatus accountStatus,
                    PharmacyStatus pharmacyStatus, boolean emailVerified,
                    int failedLoginAttempts, LocalDateTime lockedUntil,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.pharmacyName = pharmacyName;
        this.licenseNumber = licenseNumber;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accountStatus = accountStatus;
        this.pharmacyStatus = pharmacyStatus;
        this.emailVerified = emailVerified;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Role getRole() {
        return Role.PHARMACY;
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    public void lockAccount() {
        this.accountStatus = AccountStatus.LOCKED;
        this.lockedUntil = LocalDateTime.now().plusMinutes(30);
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public boolean isAccountLocked() {
        if (accountStatus == AccountStatus.LOCKED) {
            if (lockedUntil != null && LocalDateTime.now().isAfter(lockedUntil)) {
                this.accountStatus = AccountStatus.ACTIVE;
                this.failedLoginAttempts = 0;
                this.lockedUntil = null;
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isApproved() {
        return PharmacyStatus.APPROVED.equals(pharmacyStatus);
    }

     public void openShop() {
        this.isOpen = true;
        this.updatedAt = LocalDateTime.now();
    }


    public void closeShop() {
        this.isOpen = false;
        this.updatedAt = LocalDateTime.now();
    }

       public void approve() {
        this.pharmacyStatus = PharmacyStatus.APPROVED;
        this.accountStatus = AccountStatus.ACTIVE;
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
        this.updatedAt = LocalDateTime.now();
    }


    public void reject() {
        this.pharmacyStatus = PharmacyStatus.REJECTED;
        this.accountStatus = AccountStatus.LOCKED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Temporarily suspend the pharmacy for the given number of days. While
     * suspended it cannot log in and is treated as closed (hidden from search).
     */
    public void suspend(int days) {
        this.accountStatus = AccountStatus.SUSPENDED;
        this.suspendedUntil = LocalDateTime.now().plusDays(days);
        this.isOpen = false;
        this.updatedAt = LocalDateTime.now();
    }

    /** Lift an active suspension and reactivate the account. */
    public void liftSuspension() {
        this.accountStatus = AccountStatus.ACTIVE;
        this.suspendedUntil = null;
        this.updatedAt = LocalDateTime.now();
    }

    /** Record a strike (formal warning) against the pharmacy. */
    public void addStrike() {
        this.strikeCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /** Apply subscription state pushed by the Payment Service. */
    public void applySubscription(String status, LocalDateTime subscribedUntil) {
        this.subscriptionStatus = status;
        this.subscribedUntil = subscribedUntil;
        this.updatedAt = LocalDateTime.now();
    }

    /** Whether the pharmacy currently has an active subscription or trial. */
    public boolean isSubscriptionActive() {
        return subscribedUntil != null && subscribedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Whether the pharmacy is currently suspended. Lazily auto-lifts an expired
     * suspension (mirrors {@link #isAccountLocked()}).
     */
    public boolean isSuspended() {
        if (accountStatus == AccountStatus.SUSPENDED) {
            if (suspendedUntil != null && LocalDateTime.now().isAfter(suspendedUntil)) {
                this.accountStatus = AccountStatus.ACTIVE;
                this.suspendedUntil = null;
                return false;
            }
            return true;
        }
        return false;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPharmacyName() { return pharmacyName; }
    public void setPharmacyName(String pharmacyName) { this.pharmacyName = pharmacyName; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public PharmacyStatus getPharmacyStatus() { return pharmacyStatus; }
    public void setPharmacyStatus(PharmacyStatus pharmacyStatus) { this.pharmacyStatus = pharmacyStatus; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { this.isOpen = open; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public int getStrikeCount() { return strikeCount; }
    public void setStrikeCount(int strikeCount) { this.strikeCount = strikeCount; }

    public LocalDateTime getSuspendedUntil() { return suspendedUntil; }
    public void setSuspendedUntil(LocalDateTime suspendedUntil) { this.suspendedUntil = suspendedUntil; }

    public String getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }

    public LocalDateTime getSubscribedUntil() { return subscribedUntil; }
    public void setSubscribedUntil(LocalDateTime subscribedUntil) { this.subscribedUntil = subscribedUntil; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}