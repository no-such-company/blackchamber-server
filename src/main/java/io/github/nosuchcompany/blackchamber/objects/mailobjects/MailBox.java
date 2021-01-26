package io.github.nosuchcompany.blackchamber.objects.mailobjects;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MailBox {
    private List<MailFolder> folder;
}
