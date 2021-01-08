package com.swenkalski.blackchamber.objects;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MailBox {
    private List<MailFolder> folder;
}
