package io.github.nosuchcompany.blackchamber.objects.response;

import io.github.nosuchcompany.blackchamber.enums.ErrorCodes;
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
    private String errorCode;
    private String description;

    public Response(HttpStatus httpStatus, ErrorCodes errorCodes) {
        this.returncode = httpStatus.value();
        this.message = httpStatus.getReasonPhrase();
        this.errorCode = errorCodes.errorcode;
        this.description = errorCodes.description;
    }
}
