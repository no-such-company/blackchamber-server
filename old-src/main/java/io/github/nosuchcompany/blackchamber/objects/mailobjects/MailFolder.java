package io.github.nosuchcompany.blackchamber.objects.mailobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MailFolder {
    private String folderName;
    private List<Mails> mails;
}
