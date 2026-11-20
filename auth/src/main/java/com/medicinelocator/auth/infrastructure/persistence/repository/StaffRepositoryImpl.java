package com.medicinelocator.auth.infrastructure.persistence.repository;

import com.medicinelocator.auth.application.service.StaffService;
import com.medicinelocator.auth.domain.enums.Permission;
import com.medicinelocator.auth.domain.model.Staff;
import com.medicinelocator.auth.infrastructure.persistence.entity.StaffEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class StaffRepositoryImpl implements StaffService {

    private final StaffJpaRepository jpaRepository;

    public StaffRepositoryImpl(StaffJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Staff save(Staff staff) {
        StaffEntity entity = toEntity(staff);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Staff> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<Staff> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public Staff update(Staff staff) {
        StaffEntity entity = jpaRepository.findById(staff.getId())
                .orElseThrow(() -> new IllegalArgumentException("Staff not found: " + staff.getId()));
        entity.setEmail(staff.getEmail());
        entity.setPasswordHash(staff.getPasswordHash());
        entity.setFullName(staff.getFullName());
        entity.setJobTitle(staff.getJobTitle());
        entity.setPermissions(toCsv(staff.getPermissions()));
        entity.setActive(staff.isActive());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Staff> findByPharmacyId(UUID pharmacyId) {
        return jpaRepository.findByPharmacyId(pharmacyId).stream().map(this::toDomain).toList();
    }

    private StaffEntity toEntity(Staff staff) {
        StaffEntity entity = new StaffEntity();
        entity.setId(staff.getId());
        entity.setPharmacyId(staff.getPharmacyId());
        entity.setEmail(staff.getEmail());
        entity.setPasswordHash(staff.getPasswordHash());
        entity.setFullName(staff.getFullName());
        entity.setJobTitle(staff.getJobTitle());
        entity.setPermissions(toCsv(staff.getPermissions()));
        entity.setActive(staff.isActive());
        return entity;
    }

    private Staff toDomain(StaffEntity entity) {
        return new Staff(
                entity.getId(),
                entity.getPharmacyId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getFullName(),
                entity.getJobTitle(),
                fromCsv(entity.getPermissions()),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String toCsv(Set<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "";
        }
        return permissions.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    private Set<Permission> fromCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return EnumSet.noneOf(Permission.class);
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Permission::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permission.class)));
    }
}
