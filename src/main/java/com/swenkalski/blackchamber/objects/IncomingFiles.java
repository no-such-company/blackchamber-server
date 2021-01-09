package com.swenkalski.blackchamber.objects;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Getter
@Setter
public class IncomingFiles {
    private String originalFilename;
    private MultipartFile file;
    private String Hash;
    private String mailId;
    private String owner;
    private String sender;
    private File tempPath;

    public IncomingFiles(String originalFilename, MultipartFile file, String mailId, String owner, String sender) {
        this.originalFilename = originalFilename;
        this.file = file;
        this.mailId = mailId;
        this.owner = owner;
        this.sender = sender;
    }
}
