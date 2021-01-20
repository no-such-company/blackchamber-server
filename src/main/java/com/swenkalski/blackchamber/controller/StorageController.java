package com.swenkalski.blackchamber.controller;

import com.swenkalski.blackchamber.helper.FileSystemHelper;
import com.swenkalski.blackchamber.objects.mailobjects.Address;
import com.swenkalski.blackchamber.objects.mailobjects.IncomingFiles;
import com.swenkalski.blackchamber.objects.mailobjects.NewMail;
import com.swenkalski.blackchamber.objects.mailobjects.OutgoingFiles;
import com.swenkalski.blackchamber.objects.meta.OutBoxMeta;
import com.swenkalski.blackchamber.objects.response.Response;
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

    public static final String PUB_ASC = "pub.asc";
    public static final String KEY_SKR = "key.skr";

    @RequestMapping(value = "/in", method = RequestMethod.POST)
    public ResponseEntity<Response> handleNewMail(@RequestParam("attachments") List<MultipartFile> files
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
        } catch (Exception e) {
            return ResponseEntity.ok(new Response(HttpStatus.OK));
        }
    }

    /*
   Technical it is possible to send a Mail directly from Client BUT the probe of the origin would not work.
   So you must use the proper Endpoint.
    */
    @RequestMapping(value = "/inbox/mail/send", method = RequestMethod.POST)
    public ResponseEntity<Response> sendMail(@RequestParam("attachments") List<MultipartFile> files
            , @RequestParam("user") String user
            , @RequestParam("pwhash") String pwHash
            , @RequestParam("recipients") List<String> recipients
    ) throws Exception {
        UserService userService = new UserService(pwHash, new Address(user));
        if (userService.validateUser()) {
            String mailId = getHash(user + recipients.toString() + new Date().getTime());
            List<OutgoingFiles> attachments = new ArrayList<>();

            for (MultipartFile file : files) {
                attachments.add(new OutgoingFiles(
                        file.getOriginalFilename(), file, user, recipients, mailId)
                );
            }
            this.storeTempFilesForOutbound(attachments, mailId);

            this.storeOutgoingFile(attachments, mailId, user);
            for (String recipient : recipients) {
                NewMail mailHeader = new NewMail(recipient, user, mailId, new Date().getTime());
                try {
                    SendService sendService = new SendService(mailHeader, attachments);
                    sendService.send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.purgeOutgoingFiles(mailId);
            getMetaEncryptedFiles(new OutBoxMeta(new Date().getTime(), recipients ,mailId));
            return ResponseEntity.ok(new Response(HttpStatus.OK));
        }
        return ResponseEntity.ok(new Response(HttpStatus.FORBIDDEN));
    }

    @RequestMapping(value = "/inbox/setkeys", method = RequestMethod.POST)
    public ResponseEntity<Response> replaceInboxKeys(@RequestParam("user") String user,
                                                     @RequestParam("hash") String pwHash,
                                                     @RequestParam("pub") MultipartFile pubKey,
                                                     @RequestParam("priv") MultipartFile privKey) throws Exception {
        UserService userService = new UserService(pwHash, new Address(user));
        if (userService.validateUser()) {
            Address address = new Address(user);
            if (privKey.getOriginalFilename().equals(KEY_SKR) && pubKey.getOriginalFilename().equals(PUB_ASC)) {
                File dest = new File(FileSystemHelper.getUserInFolderByName(address, pubKey.getOriginalFilename()));
                copyFileUsingStream(pubKey.getResource().getFile(), dest);

                dest = new File(FileSystemHelper.getUserInFolderByName(address, privKey.getOriginalFilename()));
                copyFileUsingStream(privKey.getResource().getFile(), dest);

                return ResponseEntity.ok(new Response(HttpStatus.OK));
            }
        }
        return ResponseEntity.ok(new Response(HttpStatus.FORBIDDEN));
    }

    private void storeTempFilesForOutbound(List<OutgoingFiles> files, String mailId) throws IOException, NoSuchAlgorithmException {
        createFolder(FileSystemHelper.getTempFolderForOutboundMail(mailId));
        for (OutgoingFiles file : files) {
            File dest = new File(FileSystemHelper.getTempFolderForOutboundMail(mailId) + "/" + file.getFile().getOriginalFilename());
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

    private void storeFileTemp(List<IncomingFiles> files, NewMail mailHeader) throws Exception {
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

    private void storeOutgoingFile(List<OutgoingFiles> files, String mailId, String user) throws Exception {
        createFolder(getUserOutFolder(mailId, user));

        for (OutgoingFiles file : files) {

            File dest = new File(FileSystemHelper.getUserOutFolderWithFilename(mailId, user, file.getFile().getOriginalFilename()));
            copyFileUsingStream(file.getTempPath(), dest);
        }
    }

    private void purgeOutgoingFiles(String mailId) {
        deployMetaFile();
        purgeOutboundTempFiles(mailId);
    }

    private void deployFiles(List<IncomingFiles> files, NewMail mailHeader) throws Exception {
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

    private void purgeOutboundTempFiles(String mailId) {
        deleteDirectory(new File(FileSystemHelper.getTempFolderForOutboundMail(mailId)));
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