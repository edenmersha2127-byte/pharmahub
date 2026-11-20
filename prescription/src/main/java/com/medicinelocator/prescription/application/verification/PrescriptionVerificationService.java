package com.medicinelocator.prescription.application.verification;

import com.medicinelocator.prescription.application.service.HospitalConnectionRepository;
import com.medicinelocator.prescription.application.service.HospitalVerificationClient;
import com.medicinelocator.prescription.application.service.PrescriptionIdentityExtractor;
import com.medicinelocator.prescription.domain.model.HospitalConnection;
import com.medicinelocator.prescription.domain.model.PrescriptionIdentity;
import com.medicinelocator.prescription.domain.model.VerificationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Orchestrates the verification gate:
 *   1. read hospitalId + prescriptionId from the image (separate pass)
 *   2. look the hospital up in the integrated-hospitals directory
 *   3. call that hospital's /api/verify
 * Only a VALID outcome lets the caller proceed to medicine extraction + search.
 */
@Service
public class PrescriptionVerificationService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionVerificationService.class);

    private final PrescriptionIdentityExtractor identityExtractor;
    private final HospitalConnectionRepository connectionRepository;
    private final HospitalVerificationClient verificationClient;

    public PrescriptionVerificationService(PrescriptionIdentityExtractor identityExtractor,
                                           HospitalConnectionRepository connectionRepository,
                                           HospitalVerificationClient verificationClient) {
        this.identityExtractor = identityExtractor;
        this.connectionRepository = connectionRepository;
        this.verificationClient = verificationClient;
    }

    public VerificationOutcome verify(MultipartFile file) {
        PrescriptionIdentity identity = identityExtractor.extractIdentity(file);

        if (!identity.isPresent()) {
            log.info("Verification: could not read hospitalId/prescriptionId from image");
            return VerificationOutcome.unidentifiable();
        }

        Optional<HospitalConnection> connection =
                connectionRepository.findByHospitalId(identity.getHospitalId());

        if (connection.isEmpty() || !connection.get().isActive()) {
            log.info("Verification: hospitalId '{}' is not an active integrated hospital",
                    identity.getHospitalId());
            return VerificationOutcome.notIntegrated(identity.getHospitalId());
        }

        return verificationClient.verify(connection.get(), identity.getPrescriptionId());
    }
}
