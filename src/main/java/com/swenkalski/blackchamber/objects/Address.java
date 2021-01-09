package com.swenkalski.blackchamber.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Address {

    private String user;
    private String host;

    public Address(String user) {
        this.user = user.split("//:")[1];
        this.host = user.split("//:")[0];
    }
}
