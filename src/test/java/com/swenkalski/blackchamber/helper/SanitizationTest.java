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

    /*
        ^(?=.{8,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$
     └─────┬────┘└───┬──┘└─────┬─────┘└─────┬─────┘ └───┬───┘
           │         │         │            │           no _ or . at the end
           │         │         │            │
           │         │         │            allowed characters
           │         │         │
           │         │         no __ or _. or ._ or .. inside
           │         │
           │         no _ or . at the beginning
           │
           username is 8-20 characters long
     */


    @ParameterizedTest
    @ValueSource(strings = {"test", "'test123", ".test\\_123.", "test__test", "test..test", "test__test..test", "\r\ntest_", "'test.1234"})
    void isInvalidUsername(String sample) {
        assertFalse(com.swenkalski.blackchamber.helper.Sanitization.isUser(sample));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.test", "test.1234", "test1234", "testtest1234", "test_test", "test.test_test"})
    void isValidUsername(String sample) {
        assertTrue(com.swenkalski.blackchamber.helper.Sanitization.isUser(sample));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test",
            "'test123",
            ".test\\_123.",
            "test__test",
            "test..test",
            "test__test..test",
            "\r\ntest_", "'test.1234",
            "domain//:test//:test"}
    )
    void isInvalidAddress(String sample) {
        assertFalse(com.swenkalski.blackchamber.helper.Sanitization.isSMailAddress(sample));
    }

    @ParameterizedTest
    @ValueSource(strings = {"domain.com//:test.test",
            "domain.com//:test.1234",
            "domain.com//:test1234",
            "domain.com//:testtest1234",
            "domain.com//:test_test",
            "domain.com//:test.test_test"})
    void isValidAddress(String sample) {
        assertTrue(com.swenkalski.blackchamber.helper.Sanitization.isSMailAddress(sample));
    }
}