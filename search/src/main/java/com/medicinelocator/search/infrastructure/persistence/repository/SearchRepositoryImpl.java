package com.medicinelocator.search.infrastructure.persistence.repository;

import com.medicinelocator.search.application.service.SearchService;
import com.medicinelocator.search.domain.projection.NearbyMedicineProjection;
import com.medicinelocator.search.domain.projection.PharmacySearchView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class SearchRepositoryImpl implements SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchRepositoryImpl.class);
    private static final double METRES_PER_KM = 1000.0;

    private static final double STRICT_NAME_SIMILARITY_THRESHOLD = 0.45;

    private final SearchJpaRepository jpaRepository;

    public SearchRepositoryImpl(SearchJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Page<NearbyMedicineProjection> searchMedicinesByName(String searchTerm,
                                                                Double latitude,
                                                                Double longitude,
                                                                Double radiusKm,
                                                                Boolean requiresPrescription,
                                                                String category,
                                                                Pageable pageable) {
        int limit  = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        if (latitude != null && longitude != null) {
            Double radiusMetres = radiusKm != null ? radiusKm * METRES_PER_KM : null;

            List<Object[]> rows = jpaRepository.searchMedicinesByNameWithLocation(
                    searchTerm, STRICT_NAME_SIMILARITY_THRESHOLD, latitude, longitude, radiusMetres,
                    requiresPrescription, category, limit, offset);
            long total = jpaRepository.countSearchMedicinesByNameWithLocation(
                    searchTerm, STRICT_NAME_SIMILARITY_THRESHOLD, latitude, longitude, radiusMetres,
                    requiresPrescription, category);

            return new PageImpl<>(rows.stream().map(this::mapToNearbyProjection).toList(),
                    pageable, total);
        } else {
            List<Object[]> rows = jpaRepository.searchMedicinesByNameNoLocation(
                    searchTerm, STRICT_NAME_SIMILARITY_THRESHOLD, requiresPrescription, category, limit, offset);
            long total = jpaRepository.countSearchMedicinesByNameNoLocation(
                    searchTerm, STRICT_NAME_SIMILARITY_THRESHOLD, requiresPrescription, category);

            return new PageImpl<>(rows.stream().map(this::mapToNearbyProjectionNoDistance).toList(),
                    pageable, total);
        }
    }

    @Override
    public Page<PharmacySearchView> searchNearbyPharmacies(double latitude,
                                                           double longitude,
                                                           double radiusKm,
                                                           Pageable pageable) {
        double radiusMetres = radiusKm * METRES_PER_KM;
        int limit  = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        List<Object[]> rows = jpaRepository.findNearbyPharmacies(
                latitude, longitude, radiusMetres, limit, offset);
        long total = jpaRepository.countNearbyPharmacies(latitude, longitude, radiusMetres);

        return new PageImpl<>(rows.stream().map(this::mapToPharmacyView).toList(), pageable, total);
    }

    @Override
    public Page<NearbyMedicineProjection> searchByPrescriptionMedicines(List<String> medicineNames,
                                                                        Double latitude,
                                                                        Double longitude,
                                                                        Double radiusKm,
                                                                        Pageable pageable) {
        String[] namesArray = medicineNames.toArray(new String[0]);
        Double radiusMetres = (radiusKm != null) ? radiusKm * METRES_PER_KM : null;
        int limit  = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        List<Object[]> rows = jpaRepository.searchByPrescriptionMedicines(
                namesArray, STRICT_NAME_SIMILARITY_THRESHOLD, latitude, longitude, radiusMetres, limit, offset);
        long total = jpaRepository.countByPrescriptionMedicines(
                namesArray, STRICT_NAME_SIMILARITY_THRESHOLD, latitude, longitude, radiusMetres);

        return new PageImpl<>(rows.stream().map(this::mapToNearbyProjection).toList(),
                pageable, total);
    }

    private NearbyMedicineProjection mapToNearbyProjection(Object[] row) {
        return new NearbyMedicineProjection(
                toUUID(row[0]),
                toString(row[1]),
                toString(row[2]),
                toString(row[3]),
                toString(row[4]),
                toBoolean(row[5]),
                toBigDecimal(row[6]),
                toInt(row[7]),
                toBoolean(row[8]),
                toUUID(row[9]),
                toString(row[10]),
                toString(row[11]),
                toString(row[12]),
                toDouble(row[13]),
                toDouble(row[14]),
                toDouble(row[15])
        );
    }

    private NearbyMedicineProjection mapToNearbyProjectionNoDistance(Object[] row) {
        return new NearbyMedicineProjection(
                toUUID(row[0]),
                toString(row[1]),
                toString(row[2]),
                toString(row[3]),
                toString(row[4]),
                toBoolean(row[5]),
                toBigDecimal(row[6]),
                toInt(row[7]),
                toBoolean(row[8]),
                toUUID(row[9]),
                toString(row[10]),
                toString(row[11]),
                toString(row[12]),
                toDouble(row[13]),
                toDouble(row[14]),
                0.0
        );
    }

    private PharmacySearchView mapToPharmacyView(Object[] row) {
        return new PharmacySearchView(
                toUUID(row[0]),
                toString(row[1]),
                toString(row[2]),
                toString(row[3]),
                toDouble(row[4]),
                toDouble(row[5]),
                toDouble(row[6]),
                toInt(row[7])
        );
    }


    private UUID toUUID(Object o) {
        if (o == null) return null;
        return (o instanceof UUID u) ? u : UUID.fromString(o.toString());
    }

    private String toString(Object o) {
        return o != null ? o.toString() : null;
    }

    private boolean toBoolean(Object o) {
        if (o == null) return false;
        return (o instanceof Boolean b) ? b : Boolean.parseBoolean(o.toString());
    }

    private BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        return (o instanceof BigDecimal bd) ? bd : new BigDecimal(o.toString());
    }

    private int toInt(Object o) {
        if (o == null) return 0;
        return (o instanceof Number n) ? n.intValue() : Integer.parseInt(o.toString());
    }

    private double toDouble(Object o) {
        if (o == null) return 0.0;
        return (o instanceof Number n) ? n.doubleValue() : Double.parseDouble(o.toString());
    }
}