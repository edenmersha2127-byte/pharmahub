package com.medicinelocator.salesservice.infrastructure.persistence.repository;

import com.medicinelocator.salesservice.application.service.SaleRepository;
import com.medicinelocator.salesservice.domain.model.Sale;
import com.medicinelocator.salesservice.domain.model.SaleItem;
import com.medicinelocator.salesservice.infrastructure.persistence.entity.SaleEntity;
import com.medicinelocator.salesservice.infrastructure.persistence.entity.SaleItemEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SaleRepositoryImpl implements SaleRepository {

    private final SaleJpaRepository saleJpaRepository;
    private final SaleItemJpaRepository itemJpaRepository;

    public SaleRepositoryImpl(SaleJpaRepository saleJpaRepository,
                              SaleItemJpaRepository itemJpaRepository) {
        this.saleJpaRepository = saleJpaRepository;
        this.itemJpaRepository = itemJpaRepository;
    }

    @Override
    @Transactional
    public Sale save(Sale sale) {
        SaleEntity header = new SaleEntity();
        header.setId(sale.getId());
        header.setPharmacyId(sale.getPharmacyId());
        header.setCustomerId(sale.getCustomerId());
        header.setCashierId(sale.getCashierId());
        header.setPaymentMethod(sale.getPaymentMethod());
        header.setTotal(sale.getTotal());
        header.setCreatedAt(sale.getCreatedAt());
        SaleEntity savedHeader = saleJpaRepository.save(header);

        for (SaleItem item : sale.getItems()) {
            SaleItemEntity itemEntity = new SaleItemEntity();
            itemEntity.setId(item.getId());
            itemEntity.setSaleId(savedHeader.getId());
            itemEntity.setMedicineId(item.getMedicineId());
            itemEntity.setMedicineName(item.getMedicineName());
            itemEntity.setQuantity(item.getQuantity());
            itemEntity.setUnitPrice(item.getUnitPrice());
            itemEntity.setPurchasePrice(item.getPurchasePrice());
            item.setSaleId(savedHeader.getId());
            SaleItemEntity savedItem = itemJpaRepository.save(itemEntity);
            item.setId(savedItem.getId());
        }

        Sale result = toDomain(savedHeader);
        result.setItems(sale.getItems());
        return result;
    }

    @Override
    public Optional<Sale> findByIdAndPharmacyId(UUID saleId, UUID pharmacyId) {
        return saleJpaRepository.findByIdAndPharmacyId(saleId, pharmacyId)
                .map(entity -> {
                    Sale sale = toDomain(entity);
                    sale.setItems(loadItems(entity.getId()));
                    return sale;
                });
    }

    @Override
    public List<Sale> findByPharmacyId(UUID pharmacyId) {
        return saleJpaRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacyId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Sale> findByCustomer(UUID customerId, UUID pharmacyId) {
        return saleJpaRepository.findByCustomerIdAndPharmacyIdOrderByCreatedAtDesc(customerId, pharmacyId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Sale> findBetween(UUID pharmacyId, LocalDateTime from, LocalDateTime to) {
        return saleJpaRepository.findByPharmacyIdAndCreatedAtBetween(pharmacyId, from, to).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleItem> findItemsBetween(UUID pharmacyId, LocalDateTime from, LocalDateTime to) {
        return itemJpaRepository.findItemsBetween(pharmacyId, from, to).stream()
                .map(this::toItemDomain)
                .collect(Collectors.toList());
    }

    private List<SaleItem> loadItems(UUID saleId) {
        return itemJpaRepository.findBySaleId(saleId).stream()
                .map(this::toItemDomain)
                .collect(Collectors.toList());
    }

    private Sale toDomain(SaleEntity entity) {
        return new Sale(
                entity.getId(),
                entity.getPharmacyId(),
                entity.getCustomerId(),
                entity.getCashierId(),
                entity.getPaymentMethod(),
                entity.getTotal(),
                entity.getCreatedAt()
        );
    }

    private SaleItem toItemDomain(SaleItemEntity entity) {
        return new SaleItem(
                entity.getId(),
                entity.getSaleId(),
                entity.getMedicineId(),
                entity.getMedicineName(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getPurchasePrice()
        );
    }
}
