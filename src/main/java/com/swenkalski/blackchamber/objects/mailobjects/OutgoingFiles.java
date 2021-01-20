package com.swenkalski.blackchamber.objects.mailobjects;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Getter
@Setter
public class OutgoingFiles {
    private String originalFilename;
    private MultipartFile file;
    private String Hash;
    private String mailId;
    private List<String> reciepient;
    private String sender;
    private File tempPath;

    public OutgoingFiles(String originalFilename, MultipartFile file, String mailId, List<String> recipient, String sender) {
        this.originalFilename = originalFilename;
        this.file = file;
        this.mailId = mailId;
        this.reciepient = recipient;
        this.sender = sender;
    }
}
