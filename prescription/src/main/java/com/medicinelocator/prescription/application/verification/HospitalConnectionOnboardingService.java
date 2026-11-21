package com.medicinelocator.prescription.application.verification;

import com.medicinelocator.prescription.application.dto.request.CreateHospitalConnectionRequest;
import com.medicinelocator.prescription.application.dto.request.UpdateHospitalConnectionRequest;
import com.medicinelocator.prescription.application.service.HospitalConnectionRepository;
import com.medicinelocator.prescription.domain.exception.InvalidPrescriptionException;
import com.medicinelocator.prescription.domain.model.HospitalConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

/**
 * Admin onboarding of integrated hospitals: generates a unique hospital ID,
 * stores the connection, and (for now) logs the ID the hospital admin must
 * configure on their side. Emailing the ID can later reuse the mail setup that
 * already exists in the auth-service.
 */
@Service
public class HospitalConnectionOnboardingService {

    private static final Logger log = LoggerFactory.getLogger(HospitalConnectionOnboardingService.class);

    private static final String PREFIX = "HSP-";
    private static final String ALPHABET = "23456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final int GROUP_SIZE = 5;
    private static final int MAX_ATTEMPTS = 10;

    private final SecureRandom random = new SecureRandom();
    private final HospitalConnectionRepository connectionRepository;

    public HospitalConnectionOnboardingService(HospitalConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    public HospitalConnection onboard(CreateHospitalConnectionRequest request) {
        String hospitalId;
        if (request.hospitalId() != null && !request.hospitalId().isBlank()) {
            hospitalId = request.hospitalId().trim();
            if (connectionRepository.existsByHospitalId(hospitalId)) {
                throw new InvalidPrescriptionException("Hospital ID is already in use: " + hospitalId);
            }
        } else {
            hospitalId = generateUniqueHospitalId();
        }

        HospitalConnection connection = new HospitalConnection(
                null,
                request.name().trim(),
                request.email().trim(),
                hospitalId,
                request.baseUrl().trim(),
                request.apiKey(),
                true
        );

        HospitalConnection saved = connectionRepository.save(connection);

        // TODO: email the generated hospitalId to request.email() (reuse auth-service mail).
        log.info("Onboarded hospital '{}' -> generated hospitalId={} (send to {} to configure on their side)",
                saved.getName(), saved.getHospitalId(), saved.getEmail());

        return saved;
    }

    /**
     * Admin edit of an existing connection. The hospitalId may be changed as
     * long as no other hospital uses it. A blank apiKey keeps the stored key
     * and a null active flag keeps the current state.
     */
    public HospitalConnection update(UUID id, UpdateHospitalConnectionRequest request) {
        HospitalConnection connection = connectionRepository.findById(id)
                .orElseThrow(() -> new InvalidPrescriptionException("Hospital connection not found: " + id));

        String hospitalId = request.hospitalId().trim();
        if (!hospitalId.equals(connection.getHospitalId())
                && connectionRepository.existsByHospitalId(hospitalId)) {
            throw new InvalidPrescriptionException("Hospital ID is already in use: " + hospitalId);
        }

        String previousHospitalId = connection.getHospitalId();
        connection.setName(request.name().trim());
        connection.setEmail(request.email().trim());
        connection.setHospitalId(hospitalId);
        connection.setBaseUrl(request.baseUrl().trim());
        if (request.apiKey() != null && !request.apiKey().isBlank()) {
            connection.setApiKey(request.apiKey().trim());
        }
        if (request.active() != null) {
            connection.setActive(request.active());
        }

        HospitalConnection saved = connectionRepository.save(connection);
        log.info("Updated hospital '{}' (id={}) hospitalId {} -> {} active={}",
                saved.getName(), saved.getId(), previousHospitalId, saved.getHospitalId(), saved.isActive());
        return saved;
    }

    public List<HospitalConnection> list() {
        return connectionRepository.findAll();
    }

    private String generateUniqueHospitalId() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = PREFIX + randomGroup();
            if (!connectionRepository.existsByHospitalId(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique hospital ID");
    }

    private String randomGroup() {
        StringBuilder sb = new StringBuilder(GROUP_SIZE);
        for (int i = 0; i < GROUP_SIZE; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
