package io.github.nosuchcompany.blackchamber.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProtocolHelperTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "127.0.0.1",
            "198.162.0.1",
            "localhost"})
    void getProtocolShouldReturnHTTP(String host) {
        Assertions.assertEquals("http://", ProtocolHelper.getProtocol(host));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "domain.com",
            "mail.domain.com",
            "mail.d_o_main.com",
            "dom-ain.com"})
    void getProtocolShouldReturnHTTPS(String host) {
        Assertions.assertEquals("https://", ProtocolHelper.getProtocol(host));
    }
}