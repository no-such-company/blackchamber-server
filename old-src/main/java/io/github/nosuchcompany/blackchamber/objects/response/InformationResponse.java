package io.github.nosuchcompany.blackchamber.objects.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/*
    skalski created on 10/01/2021 inside the package - com.swenkalski.blackchamber.objects 
    Twitter: @KalskiSwen    
*/
@Getter
@Setter
public class InformationResponse {
    private int returncode;
    private String message;
    private String versionInfo;
    private String author = "Swen Kalski";
    private String copyright ="(c) 2020 by Swen Kalski";

    public InformationResponse(HttpStatus httpStatus, String version) {
        this.returncode = httpStatus.value();
        this.message = httpStatus.getReasonPhrase();
        this.versionInfo = version;
    }
}
