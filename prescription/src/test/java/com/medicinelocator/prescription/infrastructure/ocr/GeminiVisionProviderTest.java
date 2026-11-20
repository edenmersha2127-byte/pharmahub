package com.medicinelocator.prescription.infrastructure.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicinelocator.prescription.domain.exception.MalformedOcrResponseException;
import com.medicinelocator.prescription.domain.exception.OcrProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class GeminiVisionProviderTest {

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;
    private GeminiVisionProvider provider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        provider = new GeminiVisionProvider(
                "test-api-key",
                "gemini-2.0-flash",
                "https://generativelanguage.googleapis.com/v1beta/models",
                2,
                restTemplate,
                objectMapper
        );
    }

    @Test
    void getProviderName_returnsGeminiVision() {
        assertEquals("gemini_vision", provider.getProviderName());
    }

    @Test
    void extractText_success_returnsRawJsonText() {
        MultipartFile file = new MockMultipartFile(
                "file", "prescription.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        String geminiApiResponse = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          { "text": "{ \\"medicines\\": [ { \\"name\\": \\"Amoxicillin\\", \\"confidence\\": \\"HIGH\\" } ] }" }
                        ]
                      }
                    }
                  ]
                }
                """;

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(geminiApiResponse));

        String result = provider.extractText(file);

        assertNotNull(result);
        assertTrue(result.contains("Amoxicillin"));
    }

    @Test
    void extractText_emptyFile_throwsOcrProcessingException() {
        MultipartFile file = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("""
                        { "candidates": [ { "content": { "parts": [ { "text": "{}" } ] } } ] }
                        """));

        // Should not throw on empty bytes — Gemini will just receive an empty image
        assertDoesNotThrow(() -> provider.extractText(file));
    }

    @Test
    void extractText_apiReturnsErrorField_throwsOcrProcessingException() {
        MultipartFile file = new MockMultipartFile(
                "file", "prescription.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        String errorResponse = """
                { "error": { "message": "API key invalid", "code": 400 } }
                """;

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(errorResponse));

        assertThrows(OcrProcessingException.class, () -> provider.extractText(file));
    }

    @Test
    void extractText_emptyCandidatesArray_throwsMalformedOcrResponseException() {
        MultipartFile file = new MockMultipartFile(
                "file", "prescription.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        String malformedResponse = """
                { "candidates": [] }
                """;

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(malformedResponse));

        assertThrows(MalformedOcrResponseException.class, () -> provider.extractText(file));
    }

    @Test
    void extractText_clientError4xx_throwsOcrProcessingExceptionWithoutRetry() {
        MultipartFile file = new MockMultipartFile(
                "file", "prescription.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.BAD_REQUEST, "Bad Request", null, null, null));

        assertThrows(OcrProcessingException.class, () -> provider.extractText(file));

        // 4xx should NOT retry
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void extractText_networkError_retriesUpToMaxRetries() {
        MultipartFile file = new MockMultipartFile(
                "file", "prescription.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection timed out"));

        assertThrows(OcrProcessingException.class, () -> provider.extractText(file));

        // maxRetries=2 means initial attempt + 2 retries = 3 total calls
        verify(restTemplate, times(3)).postForEntity(anyString(), any(), eq(String.class));
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}