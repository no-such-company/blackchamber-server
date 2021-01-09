package com.swenkalski.blackchamber.controller;

import com.swenkalski.blackchamber.helper.FileSystemHelper;
import com.swenkalski.blackchamber.objects.Address;
import com.swenkalski.blackchamber.objects.IncomingFiles;
import com.swenkalski.blackchamber.objects.NewMail;
import com.swenkalski.blackchamber.services.ProbeService;
import com.swenkalski.blackchamber.helper.ShaHelper;
import com.swenkalski.blackchamber.services.SendService;
import com.swenkalski.blackchamber.services.UserService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.swenkalski.blackchamber.helper.FileSystemHelper.*;
import static com.swenkalski.blackchamber.helper.ShaHelper.getHash;

@RestController
public class StorageController {

    private List<IncomingFiles> files;
    private NewMail mailHeader;

    @RequestMapping(value = "/in", method = RequestMethod.POST)
    public ResponseEntity handleNewMail(@RequestParam("attachments") List<MultipartFile> files
            , @RequestParam("sender") String sender
            , @RequestParam("recipient") String recipient
            , @RequestParam("mailId") String mailId) {
        NewMail mailHeader = new NewMail(recipient, sender, mailId, new Date().getTime());
        List<IncomingFiles> attachments = new ArrayList<>();

        for (MultipartFile file : files) {
            attachments.add(new IncomingFiles(
                    file.getOriginalFilename(), file, sender, recipient, mailId)
            );
        }

        this.files = attachments;
        this.mailHeader = mailHeader;

        try {
            this.storeFileTemp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    /*
   Technical it is possible to send a Mail directly from Client BUT the probe of the origin would not work.
   So you must use the proper Endpoint.
    */
    @RequestMapping(value = "/inbox/mail/send", method = RequestMethod.POST)
    public Object sendMail(@RequestParam("attachments") List<MultipartFile> files
            , @RequestParam("user") String user
            , @RequestParam("pwhash") String pwHash
            , @RequestParam("recipients") List<String> recipients
    ) throws Exception {
        UserService userService = new UserService(pwHash, new Address(user));
        try {
            if (userService.validateUser()) {
                for (String recipient : recipients) {
                    String mailId = getHash(user + recipient + new Date().getTime());
                    NewMail mailHeader = new NewMail(recipient, user, mailId, new Date().getTime());
                    List<IncomingFiles> attachments = new ArrayList<>();

                    for (MultipartFile file : files) {
                        attachments.add(new IncomingFiles(
                                file.getOriginalFilename(), file, user, recipient, mailId)
                        );
                    }
                    this.files = attachments;
                    this.mailHeader = mailHeader;

                    SendService sendService = new SendService(user, recipient, mailId, attachments);
                    sendService.send();
                    try {
                        this.storeOutgoingFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return new ResponseEntity(HttpStatus.OK);
            }
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void storeFileTemp() throws Exception {
        createFolder(FileSystemHelper.getTempFolderForIncomingMail(mailHeader));
        for (IncomingFiles file : files) {
            File dest = new File(FileSystemHelper.getTempFolderForIncomingMail(mailHeader) + "/" + file.getFile().getOriginalFilename());
            InputStream filestream = file.getFile().getInputStream();
            java.nio.file.Files.copy(
                    filestream,
                    dest.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            IOUtils.closeQuietly(filestream);

            file.setHash(fetchHashOfFile(dest));
            file.setTempPath(dest);
        }

        ProbeService probeService = new ProbeService(files, mailHeader);
        try {
            if (probeService.sendProbeToSenderServer()) {
                deployFiles();
            }
        } catch (Exception e){
            System.out.println(e);
        }finally {
            purgeTempFiles();
        }
    }

    private void storeOutgoingFile() throws Exception {
        for (IncomingFiles file : files) {
            File dest = new File(FileSystemHelper.getTempFolderForIncomingMail(mailHeader));
            file.getFile().transferTo(dest);
            file.setHash(fetchHashOfFile(dest));
        }
        deployFiles();

        purgeTempFiles();
    }

    private void deployFiles() throws Exception {
        createFolder(getUserInFolderWithFilename(mailHeader));

        for (IncomingFiles file : files) {

            File dest = new File(FileSystemHelper.getUserInFolderWithFilename(mailHeader, file.getFile().getOriginalFilename()));
            copyFileUsingStream(file.getTempPath(), dest);
        }
        deployMetaFile();
    }

    private void purgeTempFiles() {
        deleteDirectory(new File(FileSystemHelper.getTempFolderForIncomingMail(mailHeader)));
    }

    private void deployMetaFile() {

    }

    private String fetchHashOfFile(File file) throws NoSuchAlgorithmException, IOException {
        return ShaHelper.getFileChecksum(file);
    }


    private void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}