package com.medicinelocator.inventory.infrastructure.client;

import com.medicinelocator.inventory.application.dto.request.SearchIndexRequest;
import com.medicinelocator.inventory.application.service.SearchIndexClient;
import com.medicinelocator.inventory.domain.model.Medicine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Component
public class SearchIndexRestClient implements SearchIndexClient {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexRestClient.class);

    private final RestTemplate restTemplate;
    private final String searchServiceUrl;
    private final String indexPath;

    public SearchIndexRestClient(
            RestTemplate restTemplate,
            @Value("${search-service.url:http://search-location-service:8083}") String searchServiceUrl,
            @Value("${search-service.index-path:/index/medicine}") String indexPath) {
        this.restTemplate = restTemplate;
        this.searchServiceUrl = searchServiceUrl;
        this.indexPath = indexPath;
    }

    @Override
    public void indexMedicine(Medicine medicine, String pharmacyName, String address,
                              String city, double latitude, double longitude,
                              boolean pharmacyOpen) {
        String url = searchServiceUrl + indexPath;

        SearchIndexRequest request = buildRequest(medicine, pharmacyName, address,
                city, latitude, longitude, pharmacyOpen);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SearchIndexRequest> entity = new HttpEntity<>(request, headers);

        log.debug("Calling Search Index API: url={} medicineId={} pharmacyId={}",
                url, medicine.getId(), medicine.getPharmacyId());

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Search index updated: medicineId={} pharmacyId={}",
                        medicine.getId(), medicine.getPharmacyId());
            } else {
                log.warn("Search Service returned non-2xx: status={} medicineId={}",
                        response.getStatusCode(), medicine.getId());
            }
        } catch (ResourceAccessException e) {
            log.error("Search Service unreachable: url={} medicineId={} error={}",
                    url, medicine.getId(), e.getMessage());
        } catch (RestClientException e) {
            log.error("Search index call failed: medicineId={} error={}",
                    medicine.getId(), e.getMessage(), e);
        }
    }

    private SearchIndexRequest buildRequest(Medicine medicine, String pharmacyName,
                                            String address, String city,
                                            double latitude, double longitude,
                                            boolean pharmacyOpen) {
        SearchIndexRequest request = new SearchIndexRequest();
        request.setMedicineId(medicine.getId());
        request.setMedicineName(medicine.getMedicineName());
        request.setGenericName(medicine.getGenericName());
        request.setBrandName(medicine.getBrandName());
        request.setCategory(medicine.getCategory());
        request.setDescription(medicine.getDescription());
        request.setRequiresPrescription(medicine.isRequiresPrescription());
        request.setPrice(medicine.getPrice());
        request.setStockQuantity(medicine.getStockQuantity());
        request.setAvailable(medicine.isAvailable());
        request.setActive(medicine.isActive());
        request.setPharmacyId(medicine.getPharmacyId());
        request.setPharmacyName(pharmacyName);
        request.setAddress(address);
        request.setCity(city);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setPharmacyOpen(pharmacyOpen);
        return request;
    }
}