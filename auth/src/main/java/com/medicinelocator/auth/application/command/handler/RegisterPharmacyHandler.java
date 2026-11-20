package com.medicinelocator.auth.application.command.handler;

import com.medicinelocator.auth.application.command.RegisterPharmacyCommand;
import com.medicinelocator.auth.application.service.AdminService;
import com.medicinelocator.auth.application.service.EmailService;
import com.medicinelocator.auth.application.service.NotificationNotifier;
import com.medicinelocator.auth.application.service.PasswordHasher;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.domain.enums.AccountStatus;
import com.medicinelocator.auth.domain.enums.PharmacyStatus;
import com.medicinelocator.auth.domain.model.Admin;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RegisterPharmacyHandler {

    private static final Logger log = LoggerFactory.getLogger(RegisterPharmacyHandler.class);

    private final PharmacyService pharmacyService;
    private final PasswordHasher passwordHasher;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;
    private final AdminService adminService;
    private final NotificationNotifier notificationNotifier;

    public RegisterPharmacyHandler(PharmacyService pharmacyService,
                                   PasswordHasher passwordHasher,
                                   EmailService emailService,
                                   StringRedisTemplate redisTemplate,
                                   AdminService adminService,
                                   NotificationNotifier notificationNotifier) {
        this.pharmacyService = pharmacyService;
        this.passwordHasher = passwordHasher;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
        this.adminService = adminService;
        this.notificationNotifier = notificationNotifier;
    }

    @Transactional
    public void handle(RegisterPharmacyCommand command) {
        if (pharmacyService.existsByEmail(command.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + command.getEmail());
        }

        String hashedPassword = passwordHasher.hash(command.getPassword());
        String verificationToken = UUID.randomUUID().toString();

        Pharmacy pharmacy = new Pharmacy(
                null,
                command.getEmail(),
                hashedPassword,
                command.getPharmacyName(),
                command.getLicenseNumber(),
                command.getPhoneNumber(),
                command.getAddress(),
                command.getCity(),
                command.getLatitude(),
                command.getLongitude(),
                AccountStatus.PENDING,
                PharmacyStatus.PENDING,
                false,
                0,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

       Pharmacy savedPharmacy= pharmacyService.save(pharmacy);

        redisTemplate.opsForValue().set(
                "email_verify:" + verificationToken,
                "PHARMACY:" + savedPharmacy.getId(),
                24,
                TimeUnit.HOURS
        );

        emailService.sendEmailVerification(savedPharmacy.getEmail(), verificationToken);

        log.info("Pharmacy registered successfully: email={}", pharmacy.getEmail());

        // In-app notifications (best-effort — never breaks registration).
        notificationNotifier.notify(
                savedPharmacy.getId(),
                "PHARMACY",
                "REGISTRATION_RECEIVED",
                "Registration received",
                "Your pharmacy '" + savedPharmacy.getPharmacyName()
                        + "' has been registered and is pending admin approval.",
                "/pharmacy/dashboard");

        for (Admin admin : adminService.findAll()) {
            notificationNotifier.notify(
                    admin.getId(),
                    "ADMIN",
                    "NEW_PHARMACY_PENDING",
                    "New pharmacy pending approval",
                    "Pharmacy '" + savedPharmacy.getPharmacyName()
                            + "' has registered and is awaiting approval.",
                    "/admin/pharmacies");
        }
    }
}