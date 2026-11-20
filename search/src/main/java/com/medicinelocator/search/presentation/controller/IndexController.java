package com.medicinelocator.search.presentation.controller;

import com.medicinelocator.search.application.command.IndexMedicineCommand;
import com.medicinelocator.search.application.command.handler.IndexMedicineHandler;
import com.medicinelocator.search.application.dto.request.IndexMedicineRequest;
import com.medicinelocator.search.application.mapper.IndexMapper;
import com.medicinelocator.search.application.service.UpsertSearchIndexService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/index")
public class IndexController {

    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    private final IndexMedicineHandler indexMedicineHandler;
    private final IndexMapper indexMapper;
    private final UpsertSearchIndexService upsertSearchIndexService;

    public IndexController(IndexMedicineHandler indexMedicineHandler,
                           IndexMapper indexMapper,
                           UpsertSearchIndexService upsertSearchIndexService) {
        this.indexMedicineHandler = indexMedicineHandler;
        this.indexMapper = indexMapper;
        this.upsertSearchIndexService = upsertSearchIndexService;
    }


    @PostMapping("/medicine")
    public ResponseEntity<Void> indexMedicine(@Valid @RequestBody IndexMedicineRequest request) {
        log.debug("IndexController: received index request for medicineId={} pharmacyId={}",
                request.getMedicineId(), request.getPharmacyId());

        IndexMedicineCommand command = indexMapper.toCommand(request);
        indexMedicineHandler.handle(command);

        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /index/pharmacy/{pharmacyId}/open-status?open={true|false}
     *
     * Internal endpoint called by the Auth Service when a pharmacy toggles
     * its open/close state. Flips pharmacy_open on all of that pharmacy's
     * index rows so customer search reflects it immediately.
     */
    @PatchMapping("/pharmacy/{pharmacyId}/open-status")
    public ResponseEntity<Void> updatePharmacyOpenStatus(
            @PathVariable UUID pharmacyId,
            @RequestParam boolean open) {
        log.debug("IndexController: open-status update pharmacyId={} open={}", pharmacyId, open);
        upsertSearchIndexService.setPharmacyOpen(pharmacyId, open);
        return ResponseEntity.noContent().build();
    }
}