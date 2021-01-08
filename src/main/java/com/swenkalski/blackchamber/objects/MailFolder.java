package com.swenkalski.blackchamber.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MailFolder {
    private String[] mails;
    private String folderName;
}
