package io.github.nosuchcompany.blackchamber.objects.mailobjects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfo {
    private String overallFileSize;
    private String inboxFilesAmount;
    private String outboxFilesAmount;
    private String otherFilesAmount;
}
