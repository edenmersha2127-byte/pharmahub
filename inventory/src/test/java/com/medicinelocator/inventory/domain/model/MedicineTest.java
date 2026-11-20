package com.medicinelocator.inventory.domain.model;

import com.medicinelocator.inventory.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Medicine domain model")
class MedicineTest {

    private Medicine buildMedicine(int stock) {
        return new Medicine(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Paracetamol",
                "Acetaminophen",
                "Panadol",
                "Analgesic",
                "Pain reliever",
                new BigDecimal("12.50"),
                stock,
                stock > 0,
                false,
                LocalDate.now().plusYears(2),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("deductStock() decreases stock and keeps available=true when stock remains")
    void deductStock_decreasesStock() {
        Medicine m = buildMedicine(10);
        m.deductStock(3);

        assertEquals(7, m.getStockQuantity());
        assertTrue(m.isAvailable());
    }

    @Test
    @DisplayName("deductStock() sets available=false when stock reaches zero")
    void deductStock_marksUnavailableAtZero() {
        Medicine m = buildMedicine(5);
        m.deductStock(5);

        assertEquals(0, m.getStockQuantity());
        assertFalse(m.isAvailable());
    }

    @Test
    @DisplayName("deductStock() throws InsufficientStockException when requested > available")
    void deductStock_throwsWhenInsufficient() {
        Medicine m = buildMedicine(2);
        assertThrows(InsufficientStockException.class, () -> m.deductStock(5));
    }

    @Test
    @DisplayName("deductStock() throws IllegalArgumentException for non-positive amount")
    void deductStock_throwsForNonPositive() {
        Medicine m = buildMedicine(10);
        assertThrows(IllegalArgumentException.class, () -> m.deductStock(0));
        assertThrows(IllegalArgumentException.class, () -> m.deductStock(-1));
    }

    @Test
    @DisplayName("updateStock() accepts zero (marks unavailable) and positive values")
    void updateStock_handlesZeroAndPositive() {
        Medicine m = buildMedicine(10);

        m.updateStock(0);
        assertEquals(0, m.getStockQuantity());
        assertFalse(m.isAvailable());

        m.updateStock(50);
        assertEquals(50, m.getStockQuantity());
        assertTrue(m.isAvailable());
    }

    @Test
    @DisplayName("updateStock() rejects negative quantities")
    void updateStock_rejectsNegative() {
        Medicine m = buildMedicine(10);
        assertThrows(IllegalArgumentException.class, () -> m.updateStock(-5));
    }

    @Test
    @DisplayName("updateDetails() rejects negative price")
    void updateDetails_rejectsNegativePrice() {
        Medicine m = buildMedicine(10);
        assertThrows(IllegalArgumentException.class, () -> m.updateDetails(
                "X", "X", "X", "X", "X",
                new BigDecimal("-1.00"), 10, false, LocalDate.now()
        ));
    }

    @Test
    @DisplayName("deactivate() sets active=false and available=false")
    void deactivate_deactivatesMedicine() {
        Medicine m = buildMedicine(10);
        m.deactivate();
        assertFalse(m.isActive());
        assertFalse(m.isAvailable());
    }

    @Test
    @DisplayName("belongsTo() returns true for owning pharmacy, false otherwise")
    void belongsTo_checksOwnership() {
        UUID owner = UUID.randomUUID();
        Medicine m = new Medicine(
                UUID.randomUUID(), owner, "X", "X", "X", "X", "X",
                BigDecimal.ONE, 1, true, false, LocalDate.now(),
                true, LocalDateTime.now(), LocalDateTime.now()
        );
        assertTrue(m.belongsTo(owner));
        assertFalse(m.belongsTo(UUID.randomUUID()));
    }
}
