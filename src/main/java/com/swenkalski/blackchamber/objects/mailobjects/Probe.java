package com.swenkalski.blackchamber.objects.mailobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Probe {

    private String mailId;
    private String sender;
    private String recipient;
    private String[] attachments;
}
