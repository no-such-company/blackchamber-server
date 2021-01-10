package com.swenkalski.blackchamber.controller;

import com.swenkalski.blackchamber.helper.FileSystemHelper;
import com.swenkalski.blackchamber.objects.Address;
import com.swenkalski.blackchamber.objects.IncomingFiles;
import com.swenkalski.blackchamber.objects.NewMail;
import com.swenkalski.blackchamber.objects.Response;
import com.swenkalski.blackchamber.services.ProbeService;
import com.swenkalski.blackchamber.helper.ShaHelper;
import com.swenkalski.blackchamber.services.SendService;
import com.swenkalski.blackchamber.services.UserService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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


    @RequestMapping(value = "/in", method = RequestMethod.POST)
    public ResponseEntity<Response>  handleNewMail(@RequestParam("attachments") List<MultipartFile> files
            , @RequestParam("sender") String sender
            , @RequestParam("recipient") String recipient
            , @RequestParam("mailId") String mailId) {
        try {
            NewMail mailHeader = new NewMail(recipient, sender, mailId, new Date().getTime());
            List<IncomingFiles> attachments = new ArrayList<>();


            for (MultipartFile file : files) {
                attachments.add(new IncomingFiles(
                        file.getOriginalFilename(), file, sender, recipient, mailId)
                );
            }
            this.storeFileTemp(attachments, mailHeader);
            return ResponseEntity.ok(new Response(HttpStatus.INTERNAL_SERVER_ERROR));
        } catch (Exception e){
            return ResponseEntity.ok(new Response(HttpStatus.OK));
        }
    }

    /*
   Technical it is possible to send a Mail directly from Client BUT the probe of the origin would not work.
   So you must use the proper Endpoint.
    */
    @RequestMapping(value = "/inbox/mail/send", method = RequestMethod.POST)
    public ResponseEntity<Response>  sendMail(@RequestParam("attachments") List<MultipartFile> files
            , @RequestParam("user") String user
            , @RequestParam("pwhash") String pwHash
            , @RequestParam("recipients") List<String> recipients
    ) throws Exception {
        UserService userService = new UserService(pwHash, new Address(user));
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
                this.storeTempFilesForOutbound(attachments, mailHeader);
                try {
                    SendService sendService = new SendService(mailHeader, attachments);
                    sendService.send();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    this.storeOutgoingFile(attachments, mailHeader);
                }
            }
            return ResponseEntity.ok(new Response(HttpStatus.OK));
        }
        return ResponseEntity.ok(new Response(HttpStatus.FORBIDDEN));
    }

    private void storeTempFilesForOutbound(List<IncomingFiles> files, NewMail mailHeader) throws IOException, NoSuchAlgorithmException {
        createFolder(FileSystemHelper.getTempFolderForOutboundMail(mailHeader));
        for (IncomingFiles file : files) {
            File dest = new File(FileSystemHelper.getTempFolderForOutboundMail(mailHeader) + "/" + file.getFile().getOriginalFilename());
            InputStream filestream = file.getFile().getInputStream();
            java.nio.file.Files.copy(
                    filestream,
                    dest.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            IOUtils.closeQuietly(filestream);

            file.setHash(fetchHashOfFile(dest));
            file.setTempPath(dest);
        }
    }

    private void storeFileTemp(List<IncomingFiles>files, NewMail mailHeader) throws Exception {
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
                deployFiles(files, mailHeader);
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            purgeTempFiles(mailHeader);
        }
    }

    private void storeOutgoingFile(List<IncomingFiles>files, NewMail mailHeader) throws Exception {
        createFolder(getUserOutFolder(mailHeader));

        for (IncomingFiles file : files) {

            File dest = new File(FileSystemHelper.getUserOutFolderWithFilename(mailHeader, file.getFile().getOriginalFilename()));
            copyFileUsingStream(file.getTempPath(), dest);
        }
        deployMetaFile();

        purgeOutboundTempFiles(mailHeader);
    }

    private void deployFiles(List<IncomingFiles>files, NewMail mailHeader) throws Exception {
        createFolder(getUserInFolderWithFilename(mailHeader));

        for (IncomingFiles file : files) {

            File dest = new File(FileSystemHelper.getUserInFolderWithFilename(mailHeader, file.getFile().getOriginalFilename()));
            copyFileUsingStream(file.getTempPath(), dest);
        }
        deployMetaFile();
    }

    private void purgeTempFiles(NewMail mailHeader) {
        deleteDirectory(new File(FileSystemHelper.getTempFolderForIncomingMail(mailHeader)));
    }

    private void purgeOutboundTempFiles(NewMail mailHeader) {
        deleteDirectory(new File(FileSystemHelper.getTempFolderForOutboundMail(mailHeader)));
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