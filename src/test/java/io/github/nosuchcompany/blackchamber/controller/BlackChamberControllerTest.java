package io.github.nosuchcompany.blackchamber.controller;
/* 
    skalski created on 19/02/2021 inside the package - io.github.nosuchcompany.blackchamber.controller 
    Twitter: @KalskiSwen    
*/

import io.github.nosuchcompany.blackchamber.objects.response.InformationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertThrows;

public class BlackChamberControllerTest {

    BlackChamberController BCController = new BlackChamberController();

    @Test
    public void testWelcome() {
        ResponseEntity<InformationResponse> result = BCController.welcome();
        assertEquals(result.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testFetchInboxInformation_shouldReturnException() throws Exception {
        assertThrows(Exception.class, () -> {BCController.fetchInboxInformation("test", "test");});
    }

    @Test
    public void testFetchInboxInformation() throws Exception {
        Object result = BCController.fetchInboxInformation("test.com//:test.test", "test");
        assertEquals(result, new ResponseEntity(HttpStatus.BAD_REQUEST));
    }
}
