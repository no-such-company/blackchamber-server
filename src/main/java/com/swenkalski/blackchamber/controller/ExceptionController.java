package com.swenkalski.blackchamber.controller;
/* 
    skalski created on 10/01/2021 inside the package - com.swenkalski.blackchamber.controller 
    Twitter: @KalskiSwen    
*/

import com.swenkalski.blackchamber.objects.response.InformationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ExceptionController implements ErrorController {

    @Value("${bc.version}")
    private String version;

    @RequestMapping("/error")
    public ResponseEntity<InformationResponse> handleError(HttpServletRequest request) {
        return ResponseEntity.ok(new InformationResponse(HttpStatus.FORBIDDEN, version));
    }

    @Override
    public String getErrorPath() {
        return null;
    }
}
