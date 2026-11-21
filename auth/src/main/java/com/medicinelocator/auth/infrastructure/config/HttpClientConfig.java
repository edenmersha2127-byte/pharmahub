package com.medicinelocator.auth.infrastructure.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Outbound HTTP client used for service-to-service calls
 * (e.g. propagating pharmacy open/close state to the Search Service).
 *
 * The JDK HttpClient is chosen explicitly. With no Apache/Jetty/OkHttp client
 * on the classpath, Spring Boot falls back to HttpURLConnection, which cannot
 * send PATCH ("Invalid HTTP method: PATCH") — so the open-status sync to the
 * Search Service failed on every call and pharmacies never appeared in search.
 */
@Configuration
public class HttpClientConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(() -> {
                    HttpClient httpClient = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(3))
                            .build();
                    JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
                    factory.setReadTimeout(Duration.ofSeconds(5));
                    return factory;
                })
                .build();
    }
}
