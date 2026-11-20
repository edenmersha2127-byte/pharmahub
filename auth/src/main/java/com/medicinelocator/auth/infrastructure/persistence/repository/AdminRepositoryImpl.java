package com.medicinelocator.auth.infrastructure.persistence.repository;

import com.medicinelocator.auth.application.service.AdminService;
import com.medicinelocator.auth.domain.model.Admin;
import com.medicinelocator.auth.infrastructure.persistence.entity.AdminEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AdminRepositoryImpl implements AdminService {

    private final AdminJpaRepository jpaRepository;

    public AdminRepositoryImpl(AdminJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Admin> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<Admin> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Admin> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Admin save(Admin admin) {
        return toDomain(jpaRepository.save(toEntity(admin)));
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    private AdminEntity toEntity(Admin admin) {
        AdminEntity entity = new AdminEntity();
        entity.setId(admin.getId());
        entity.setEmail(admin.getEmail());
        entity.setPasswordHash(admin.getPasswordHash());
        entity.setFirstName(admin.getFirstName());
        entity.setLastName(admin.getLastName());
        entity.setRole(admin.getRole());
        entity.setAccountStatus(admin.getAccountStatus());
        return entity;
    }

    private Admin toDomain(AdminEntity entity) {
        Admin admin = new Admin(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getAccountStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
        admin.setRole(entity.getRole());
        return admin;
    }
}