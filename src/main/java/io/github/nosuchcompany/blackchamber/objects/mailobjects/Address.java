package io.github.nosuchcompany.blackchamber.objects.mailobjects;

import lombok.Getter;
import lombok.Setter;

import static io.github.nosuchcompany.blackchamber.helper.Sanitization.isSMailAddress;

@Getter
@Setter
public class Address {

    private String user;
    private String host;

    public Address(String user) throws Exception {
        if(!isSMailAddress(user)){
            throw new Exception();
        }
        this.user = user.split("//:")[1];
        this.host = user.split("//:")[0];
    }
}
