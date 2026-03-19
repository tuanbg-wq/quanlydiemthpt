package com.quanly.webdiem.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    private final PasswordHasher passwordHasher = new PasswordHasher();

    @Test
    void shouldMatchPasswordWithAtSymbolAfterHashing() {
        String rawPassword = "12345@";
        String encodedPassword = passwordHasher.encode(rawPassword);

        assertTrue(passwordHasher.matches(rawPassword, encodedPassword));
        assertFalse(passwordHasher.matches("123456@", encodedPassword));
    }

    @Test
    void shouldSupportLegacyPlainTextPasswordForBackwardCompatibility() {
        assertTrue(passwordHasher.matches("12345@", "12345@"));
        assertFalse(passwordHasher.matches("123456@", "12345@"));
    }
}
