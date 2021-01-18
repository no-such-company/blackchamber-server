package com.swenkalski.blackchamber.helper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SanitizationTest {

    @ParameterizedTest
    @ValueSource(strings = {"test", "test_", "_test_123"})
    void isValidFolderNamePattern(String sample) {
        assertTrue(com.swenkalski.blackchamber.helper.Sanitization.isValidFolderNamePattern(sample));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.", "'test_", "/_test\\_123", "\\test", "/test.", "\ntest", "\r\ntest"})
    void isInvalidFolderNamePattern(String sample) {
        assertFalse(com.swenkalski.blackchamber.helper.Sanitization.isValidFolderNamePattern(sample));
    }
}