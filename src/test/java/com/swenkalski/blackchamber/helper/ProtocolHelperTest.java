package com.swenkalski.blackchamber.helper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.swenkalski.blackchamber.helper.ProtocolHelper.getProtocol;
import static org.junit.jupiter.api.Assertions.*;

class ProtocolHelperTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "127.0.0.1",
            "198.162.0.1",
            "localhost"})
    void getProtocolShouldReturnHTTP(String host) {
        assertEquals("http://", getProtocol(host));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "domain.com",
            "mail.domain.com",
            "mail.d_o_main.com",
            "dom-ain.com"})
    void getProtocolShouldReturnHTTPS(String host) {
        assertEquals("https://", getProtocol(host));
    }
}