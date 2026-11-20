package com.medicinelocator.auth.infrastructure.client;

import com.medicinelocator.auth.application.service.SearchIndexNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

/**
 * Calls the Search Service to keep the denormalized index in sync with
 * pharmacy open/close changes owned by the Auth Service.
 *
 * Target: PATCH {search-service.url}{pharmacy-status-path}/{pharmacyId}/open-status?open={true|false}
 */
@Component
public class SearchIndexRestClient implements SearchIndexNotifier {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexRestClient.class);

    private final RestTemplate restTemplate;
    private final String searchServiceUrl;
    private final String pharmacyStatusPath;

    public SearchIndexRestClient(
            RestTemplate restTemplate,
            @Value("${search-service.url:http://localhost:8083}") String searchServiceUrl,
            @Value("${search-service.pharmacy-status-path:/index/pharmacy}") String pharmacyStatusPath) {
        this.restTemplate = restTemplate;
        this.searchServiceUrl = searchServiceUrl;
        this.pharmacyStatusPath = pharmacyStatusPath;
    }

    @Override
    public void updatePharmacyOpenStatus(UUID pharmacyId, boolean open) {
        String url = UriComponentsBuilder
                .fromHttpUrl(searchServiceUrl + pharmacyStatusPath + "/" + pharmacyId + "/open-status")
                .queryParam("open", open)
                .toUriString();

        log.debug("Notifying Search Service of open-status change: pharmacyId={} open={}", pharmacyId, open);

        try {
            ResponseEntity<Void> response =
                    restTemplate.exchange(url, HttpMethod.PATCH, null, Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Search index open-status synced: pharmacyId={} open={}", pharmacyId, open);
            } else {
                log.warn("Search Service returned non-2xx on open-status sync: status={} pharmacyId={}",
                        response.getStatusCode(), pharmacyId);
            }
        } catch (ResourceAccessException e) {
            log.error("Search Service unreachable for open-status sync: url={} pharmacyId={} error={}",
                    url, pharmacyId, e.getMessage());
        } catch (RestClientException e) {
            log.error("Open-status sync call failed: pharmacyId={} error={}",
                    pharmacyId, e.getMessage(), e);
        }
    }
}
