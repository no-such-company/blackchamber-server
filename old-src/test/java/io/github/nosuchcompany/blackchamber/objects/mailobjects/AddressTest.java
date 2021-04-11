package io.github.nosuchcompany.blackchamber.objects.mailobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    void validAddress() throws Exception {
        Address address = new Address("some.com//:some.one_else");
        assertEquals(address.getHost(), "some.com");
        assertEquals(address.getUser(), "some.one_else");
    }

    @Test
    void invalidAddress() throws Exception {
        assertThrows(Exception.class, () -> {
            new Address("some.com//:some..one_else").getHost();
        });
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "some.com//:some.one_else",
            "localhost//:some.one_else",
            "127.0.0.1//:some.one_else"}
    )
    void validAddress(String testAddress) throws Exception {
        Address address = new Address(testAddress);
        assertEquals(address.getHost(), testAddress.split("//:")[0]);
        assertEquals(address.getUser(), testAddress.split("//:")[1]);
    }
}