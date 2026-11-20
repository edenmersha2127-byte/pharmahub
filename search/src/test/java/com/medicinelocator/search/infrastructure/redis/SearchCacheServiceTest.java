package com.medicinelocator.search.infrastructure.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("SearchCacheService — pure key-building logic")
class SearchCacheServiceTest {

    private SearchCacheService cache;

    @BeforeEach
    void setUp() {
        cache = new SearchCacheService(mock(StringRedisTemplate.class), 60L, 30L);
    }

    @Test
    @DisplayName("buildMedicineCacheKey() normalizes search term to lowercase and trims whitespace")
    void buildMedicineCacheKey_normalizesTerm() {
        String key = cache.buildMedicineCacheKey("  Paracetamol  ", 9.0, 38.7, 5.0,
                false, "Analgesic", 0, 20);
        assertTrue(key.startsWith("paracetamol:"), "search term should be lowercased + trimmed: " + key);
    }

    @Test
    @DisplayName("buildMedicineCacheKey() produces identical keys for the same inputs (stable hash)")
    void buildMedicineCacheKey_isStable() {
        String key1 = cache.buildMedicineCacheKey("aspirin", 9.0, 38.7, 5.0,
                true, "NSAID", 1, 20);
        String key2 = cache.buildMedicineCacheKey("aspirin", 9.0, 38.7, 5.0,
                true, "NSAID", 1, 20);
        assertEquals(key1, key2);
    }

    @Test
    @DisplayName("buildMedicineCacheKey() produces different keys when search term changes")
    void buildMedicineCacheKey_differsForDifferentTerms() {
        String k1 = cache.buildMedicineCacheKey("aspirin", 9.0, 38.7, 5.0,
                false, null, 0, 20);
        String k2 = cache.buildMedicineCacheKey("ibuprofen", 9.0, 38.7, 5.0,
                false, null, 0, 20);
        assertNotEquals(k1, k2);
    }

    @Test
    @DisplayName("buildMedicineCacheKey() treats null lat/lng/radius as 0.0")
    void buildMedicineCacheKey_handlesNullLocation() {
        String key = cache.buildMedicineCacheKey("aspirin", null, null, null,
                null, null, 0, 20);
        assertNotNull(key);
        assertTrue(key.contains("0.0000"), "null lat/lng should become 0.0000: " + key);
    }
}
