package io.github.nosuchcompany.blackchamber.helper;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AlternateHostHelperTest {

    @Test
    void test_getFinalDestinationHost_NoRedirFileAvailable() throws IOException {
        assertEquals(AlternateHostHelper.getFinalDestinationHost("test.url"), "test.url");
    }
}