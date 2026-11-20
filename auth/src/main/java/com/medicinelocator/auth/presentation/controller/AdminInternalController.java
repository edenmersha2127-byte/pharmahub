package com.medicinelocator.auth.presentation.controller;

import com.medicinelocator.auth.application.service.AdminService;
import com.medicinelocator.auth.domain.enums.Role;
import com.medicinelocator.auth.domain.model.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Internal REST endpoint exposing admin account ids.
 *
 * Route: GET /internal/admins
 *
 * Consumed by the Report Service to fan out "pharmacy flagged for review"
 * alerts to every admin. Service-to-service only — not exposed via the gateway.
 */
@RestController
@RequestMapping("/internal/admins")
public class AdminInternalController {

    private static final Logger log = LoggerFactory.getLogger(AdminInternalController.class);

    private final AdminService adminService;

    public AdminInternalController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * @param role optional role filter (e.g. REPORT_ADMIN). When omitted,
     *             returns every admin id. Invalid values yield an empty list.
     */
    @GetMapping
    public ResponseEntity<List<UUID>> getAdminIds(@RequestParam(required = false) String role) {
        Role roleFilter = parseRole(role);
        List<UUID> ids = adminService.findAll().stream()
                .filter(a -> roleFilter == null || a.getRole() == roleFilter)
                .map(Admin::getId)
                .toList();
        log.debug("Internal admin-ids request: role={} count={}", role, ids.size());
        return ResponseEntity.ok(ids);
    }

    private Role parseRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown role filter requested for admin-ids: {}", role);
            return null;
        }
    }
}
