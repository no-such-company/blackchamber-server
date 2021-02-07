package io.github.nosuchcompany.blackchamber.objects.mailobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Mails {
    private String mailId;
    private String[] attachments;
    private String[] mailDescriptors;
}
