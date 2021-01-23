package io.github.nosuchcompany.blackchamber.objects.mailobjects;

import org.junit.jupiter.api.Test;

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

}