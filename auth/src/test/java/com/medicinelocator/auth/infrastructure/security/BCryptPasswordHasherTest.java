package com.medicinelocator.auth.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BCryptPasswordHasher")
class BCryptPasswordHasherTest {

    private BCryptPasswordHasher hasher;

    @BeforeEach
    void setUp() {
        hasher = new BCryptPasswordHasher();
    }

    @Test
    @DisplayName("hash() produces a non-null, non-empty hash different from the raw password")
    void hash_producesValidHash() {
        String raw = "MyStrongP@ssw0rd";
        String hashed = hasher.hash(raw);

        assertNotNull(hashed);
        assertFalse(hashed.isEmpty());
        assertNotEquals(raw, hashed);
        assertTrue(hashed.startsWith("$2"), "BCrypt hashes start with $2");
    }

    @Test
    @DisplayName("matches() returns true for the original raw password")
    void matches_returnsTrueForCorrectPassword() {
        String raw = "correct-password-123";
        String hashed = hasher.hash(raw);

        assertTrue(hasher.matches(raw, hashed));
    }

    @Test
    @DisplayName("matches() returns false for a wrong password")
    void matches_returnsFalseForWrongPassword() {
        String hashed = hasher.hash("real-password");

        assertFalse(hasher.matches("wrong-password", hashed));
    }

    @Test
    @DisplayName("hashing the same password twice produces different hashes (salting works)")
    void hash_isSaltedAndUnique() {
        String raw = "samePassword";
        String hash1 = hasher.hash(raw);
        String hash2 = hasher.hash(raw);

        assertNotEquals(hash1, hash2, "Two hashes of the same password must differ due to random salt");
        assertTrue(hasher.matches(raw, hash1));
        assertTrue(hasher.matches(raw, hash2));
    }
}
