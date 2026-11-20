package com.medicinelocator.auth.domain.model;

import com.medicinelocator.auth.domain.enums.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Customer domain model")
class CustomerTest {

    private Customer buildCustomer(AccountStatus status, LocalDateTime lockedUntil, int failedAttempts) {
        return new Customer(
                UUID.randomUUID(),
                "test@example.com",
                "hash",
                "First",
                "Last",
                "0911000000",
                status,
                true,
                failedAttempts,
                lockedUntil,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("incrementFailedLoginAttempts() increments the counter by 1")
    void incrementFailedLoginAttempts_incrementsCounter() {
        Customer customer = buildCustomer(AccountStatus.ACTIVE, null, 2);
        customer.incrementFailedLoginAttempts();
        assertEquals(3, customer.getFailedLoginAttempts());
    }

    @Test
    @DisplayName("lockAccount() sets status=LOCKED and lockedUntil to ~30 minutes in the future")
    void lockAccount_setsLockedStatus() {
        Customer customer = buildCustomer(AccountStatus.ACTIVE, null, 5);
        customer.lockAccount();

        assertEquals(AccountStatus.LOCKED, customer.getAccountStatus());
        assertNotNull(customer.getLockedUntil());
        assertTrue(customer.getLockedUntil().isAfter(LocalDateTime.now().plusMinutes(29)));
        assertTrue(customer.getLockedUntil().isBefore(LocalDateTime.now().plusMinutes(31)));
    }

    @Test
    @DisplayName("resetFailedLoginAttempts() zeroes counter and clears lockedUntil")
    void resetFailedLoginAttempts_clearsFailureState() {
        Customer customer = buildCustomer(AccountStatus.ACTIVE, LocalDateTime.now().plusMinutes(20), 4);
        customer.resetFailedLoginAttempts();

        assertEquals(0, customer.getFailedLoginAttempts());
        assertNull(customer.getLockedUntil());
    }

    @Test
    @DisplayName("isAccountLocked() returns true when status=LOCKED and lockedUntil is in the future")
    void isAccountLocked_returnsTrueWhenLocked() {
        Customer customer = buildCustomer(AccountStatus.LOCKED, LocalDateTime.now().plusMinutes(10), 5);
        assertTrue(customer.isAccountLocked());
    }

    @Test
    @DisplayName("isAccountLocked() auto-unlocks when lockedUntil has passed")
    void isAccountLocked_autoUnlocksWhenExpired() {
        Customer customer = buildCustomer(AccountStatus.LOCKED, LocalDateTime.now().minusMinutes(1), 5);

        assertFalse(customer.isAccountLocked());
        assertEquals(AccountStatus.ACTIVE, customer.getAccountStatus());
        assertEquals(0, customer.getFailedLoginAttempts());
        assertNull(customer.getLockedUntil());
    }

    @Test
    @DisplayName("isAccountLocked() returns false for an active account")
    void isAccountLocked_returnsFalseForActiveAccount() {
        Customer customer = buildCustomer(AccountStatus.ACTIVE, null, 0);
        assertFalse(customer.isAccountLocked());
    }

    @Test
    @DisplayName("verifyEmail() flips emailVerified to true")
    void verifyEmail_marksEmailVerified() {
        Customer customer = buildCustomer(AccountStatus.PENDING, null, 0);
        customer.setEmailVerified(false);
        customer.verifyEmail();
        assertTrue(customer.isEmailVerified());
    }
}
