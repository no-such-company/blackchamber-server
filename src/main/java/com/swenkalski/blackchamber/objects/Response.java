package com.swenkalski.blackchamber.objects;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/*
    skalski created on 10/01/2021 inside the package - com.swenkalski.blackchamber.objects 
    Twitter: @KalskiSwen    
*/
@Getter
@Setter
public class Response {
    private int returncode;
    private String message;

    public Response(HttpStatus httpStatus) {
        this.returncode = httpStatus.value();
        this.message = httpStatus.getReasonPhrase();
    }
}
