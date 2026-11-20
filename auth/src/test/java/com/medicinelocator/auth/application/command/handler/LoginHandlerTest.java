package com.medicinelocator.auth.application.command.handler;

import com.medicinelocator.auth.application.command.LoginCommand;
import com.medicinelocator.auth.application.dto.response.AuthResponse;
import com.medicinelocator.auth.application.service.AdminService;
import com.medicinelocator.auth.application.service.CustomerService;
import com.medicinelocator.auth.application.service.PasswordHasher;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.application.service.TokenService;
import com.medicinelocator.auth.domain.enums.AccountStatus;
import com.medicinelocator.auth.domain.enums.PharmacyStatus;
import com.medicinelocator.auth.domain.enums.Role;
import com.medicinelocator.auth.domain.exception.AccountLockedException;
import com.medicinelocator.auth.domain.exception.EmailNotVerifiedException;
import com.medicinelocator.auth.domain.exception.PharmacyNotApprovedException;
import com.medicinelocator.auth.domain.model.Customer;
import com.medicinelocator.auth.domain.model.Pharmacy;
import com.medicinelocator.auth.domain.model.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginHandler")
class LoginHandlerTest {

    @Mock private CustomerService customerService;
    @Mock private PharmacyService pharmacyService;
    @Mock private AdminService adminService;
    @Mock private PasswordHasher passwordHasher;
    @Mock private TokenService tokenService;

    @InjectMocks
    private LoginHandler loginHandler;

    @BeforeEach
    void setUp() {
        // @Value defaults aren't applied in pure Mockito tests — inject manually
        ReflectionTestUtils.setField(loginHandler, "accessTokenExpiration", 900_000L);
    }

    private Customer activeVerifiedCustomer() {
        return new Customer(
                UUID.randomUUID(),
                "user@example.com",
                "hashed-pw",
                "Test",
                "User",
                "0911000000",
                AccountStatus.ACTIVE,
                true,
                0,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Pharmacy buildPharmacy(AccountStatus accountStatus,
                                   PharmacyStatus pharmacyStatus,
                                   boolean emailVerified) {
        return new Pharmacy(
                UUID.randomUUID(),
                "pharmacy@example.com",
                "hashed-pw",
                "ABC Pharmacy",
                "LIC-001",
                "0911000000",
                "1 Main St",
                "Addis Ababa",
                9.0,
                38.7,
                accountStatus,
                pharmacyStatus,
                emailVerified,
                0,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("throws IllegalArgumentException when no user with that email exists")
    void throws_whenEmailNotFound() {
        when(customerService.findByEmail("none@example.com")).thenReturn(Optional.empty());
        when(pharmacyService.findByEmail("none@example.com")).thenReturn(Optional.empty());
        when(adminService.findByEmail("none@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> loginHandler.handle(new LoginCommand("none@example.com", "x")));
    }

    @Test
    @DisplayName("customer login: returns AuthResponse with CUSTOMER role on success")
    void customerLogin_success() {
        Customer customer = activeVerifiedCustomer();
        when(customerService.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(passwordHasher.matches("correct-pw", "hashed-pw")).thenReturn(true);
        when(tokenService.generateAccessToken(any(UUID.class), eq(customer.getEmail()), eq(Role.CUSTOMER)))
                .thenReturn("access-jwt");

        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID(), "rt-hash", customer.getId(), Role.CUSTOMER,
                false, false, LocalDateTime.now().plusDays(7), LocalDateTime.now()
        );
        when(tokenService.generateRefreshToken(customer.getId(), Role.CUSTOMER)).thenReturn(refreshToken);

        AuthResponse response = loginHandler.handle(new LoginCommand(customer.getEmail(), "correct-pw"));

        assertNotNull(response);
        assertEquals("access-jwt", response.getAccessToken());
        assertEquals("rt-hash", response.getRefreshToken());
        assertEquals("CUSTOMER", response.getRole());
        verify(customerService).update(customer);
    }

    @Test
    @DisplayName("customer login: throws AccountLockedException when account is locked")
    void customerLogin_throwsWhenLocked() {
        Customer customer = activeVerifiedCustomer();
        customer.setAccountStatus(AccountStatus.LOCKED);
        customer.setLockedUntil(LocalDateTime.now().plusMinutes(20));

        when(customerService.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));

        assertThrows(AccountLockedException.class,
                () -> loginHandler.handle(new LoginCommand(customer.getEmail(), "any")));
    }

    @Test
    @DisplayName("customer login: throws EmailNotVerifiedException when email not verified")
    void customerLogin_throwsWhenEmailNotVerified() {
        Customer customer = activeVerifiedCustomer();
        customer.setEmailVerified(false);

        when(customerService.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(passwordHasher.matches("pw", "hashed-pw")).thenReturn(true);

        assertThrows(EmailNotVerifiedException.class,
                () -> loginHandler.handle(new LoginCommand(customer.getEmail(), "pw")));
    }

    @Test
    @DisplayName("customer login: increments failed attempts on wrong password")
    void customerLogin_incrementsFailedAttempts() {
        Customer customer = activeVerifiedCustomer();
        when(customerService.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(passwordHasher.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> loginHandler.handle(new LoginCommand(customer.getEmail(), "wrong")));

        assertEquals(1, customer.getFailedLoginAttempts());
        verify(customerService).update(customer);
    }

    @Test
    @DisplayName("customer login: locks account after 5 failed attempts")
    void customerLogin_locksAfter5Failures() {
        Customer customer = activeVerifiedCustomer();
        customer.setFailedLoginAttempts(4); // 5th failure will lock
        when(customerService.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(passwordHasher.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> loginHandler.handle(new LoginCommand(customer.getEmail(), "wrong")));

        assertEquals(5, customer.getFailedLoginAttempts());
        assertEquals(AccountStatus.LOCKED, customer.getAccountStatus());
    }

    @Test
    @DisplayName("pharmacy login: throws PharmacyNotApprovedException when status is PENDING")
    void pharmacyLogin_throwsWhenNotApproved() {
        Pharmacy pharmacy = buildPharmacy(AccountStatus.PENDING, PharmacyStatus.PENDING, true);
        when(customerService.findByEmail(pharmacy.getEmail())).thenReturn(Optional.empty());
        when(pharmacyService.findByEmail(pharmacy.getEmail())).thenReturn(Optional.of(pharmacy));
        when(passwordHasher.matches("pw", "hashed-pw")).thenReturn(true);

        assertThrows(PharmacyNotApprovedException.class,
                () -> loginHandler.handle(new LoginCommand(pharmacy.getEmail(), "pw")));
    }

    @Test
    @DisplayName("pharmacy login: succeeds when approved, verified, and active")
    void pharmacyLogin_success() {
        Pharmacy pharmacy = buildPharmacy(AccountStatus.ACTIVE, PharmacyStatus.APPROVED, true);
        when(customerService.findByEmail(pharmacy.getEmail())).thenReturn(Optional.empty());
        when(pharmacyService.findByEmail(pharmacy.getEmail())).thenReturn(Optional.of(pharmacy));
        when(passwordHasher.matches("pw", "hashed-pw")).thenReturn(true);
        when(tokenService.generateAccessToken(any(UUID.class), eq(pharmacy.getEmail()), eq(Role.PHARMACY)))
                .thenReturn("pharmacy-jwt");
        when(tokenService.generateRefreshToken(pharmacy.getId(), Role.PHARMACY))
                .thenReturn(new RefreshToken(
                        UUID.randomUUID(), "rt", pharmacy.getId(), Role.PHARMACY,
                        false, false, LocalDateTime.now().plusDays(7), LocalDateTime.now()));

        AuthResponse response = loginHandler.handle(new LoginCommand(pharmacy.getEmail(), "pw"));

        assertEquals("pharmacy-jwt", response.getAccessToken());
        assertEquals("PHARMACY", response.getRole());
    }
}
