package com.medicinelocator.prescription.infrastructure.persistence.repository;

import com.medicinelocator.prescription.application.service.HospitalConnectionRepository;
import com.medicinelocator.prescription.domain.model.HospitalConnection;
import com.medicinelocator.prescription.infrastructure.persistence.entity.HospitalConnectionEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class HospitalConnectionRepositoryImpl implements HospitalConnectionRepository {

    private final HospitalConnectionJpaRepository jpaRepository;

    public HospitalConnectionRepositoryImpl(HospitalConnectionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public HospitalConnection save(HospitalConnection connection) {
        HospitalConnectionEntity entity = (connection.getId() != null)
                ? jpaRepository.findById(connection.getId()).orElseGet(HospitalConnectionEntity::new)
                : new HospitalConnectionEntity();
        entity.setName(connection.getName());
        entity.setEmail(connection.getEmail());
        entity.setHospitalId(connection.getHospitalId());
        entity.setBaseUrl(connection.getBaseUrl());
        entity.setApiKey(connection.getApiKey());
        entity.setActive(connection.isActive());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<HospitalConnection> findByHospitalId(String hospitalId) {
        return jpaRepository.findByHospitalId(hospitalId).map(this::toDomain);
    }

    @Override
    public List<HospitalConnection> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByHospitalId(String hospitalId) {
        return jpaRepository.existsByHospitalId(hospitalId);
    }

    private HospitalConnection toDomain(HospitalConnectionEntity entity) {
        return new HospitalConnection(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getHospitalId(),
                entity.getBaseUrl(),
                entity.getApiKey(),
                entity.isActive()
        );
    }
}
