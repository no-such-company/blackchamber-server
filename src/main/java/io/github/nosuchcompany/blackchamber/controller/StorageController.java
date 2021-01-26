package io.github.nosuchcompany.blackchamber.controller;

import com.google.gson.Gson;
import io.github.nosuchcompany.blackchamber.helper.FileSystemHelper;
import io.github.nosuchcompany.blackchamber.objects.mailobjects.Address;
import io.github.nosuchcompany.blackchamber.objects.mailobjects.IncomingFiles;
import io.github.nosuchcompany.blackchamber.objects.mailobjects.NewMail;
import io.github.nosuchcompany.blackchamber.objects.mailobjects.OutgoingFiles;
import io.github.nosuchcompany.blackchamber.objects.meta.InBoxMeta;
import io.github.nosuchcompany.blackchamber.objects.meta.OutBoxMeta;
import io.github.nosuchcompany.blackchamber.objects.response.Response;
import io.github.nosuchcompany.blackchamber.services.ProbeService;
import io.github.nosuchcompany.blackchamber.helper.ShaHelper;
import io.github.nosuchcompany.blackchamber.services.SendService;
import io.github.nosuchcompany.blackchamber.services.UserService;
import io.github.nosuchcompany.pgplug.sign.SignedFileProcessor;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static io.github.nosuchcompany.blackchamber.constants.Constants.*;
import static io.github.nosuchcompany.pgplug.utils.PGPUtils.encrypt;
import static io.github.nosuchcompany.pgplug.utils.PGPUtils.readPublicKey;

@RestController
public class StorageController {

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
    public ResponseEntity<Response> sendMail_multipleRecipients(@RequestParam("attachments") List<MultipartFile> files
            , @RequestParam("user") String user
            , @RequestParam("pwhash") String pwHash
            , @RequestParam("recipients") List<String> recipients
    ) throws Exception {
        UserService userService = new UserService(pwHash, new Address(user));
        if (userService.validateUser()) {
            String mailId = ShaHelper.getHash(user + recipients.toString() + new Date().getTime());
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
            Gson gson = new Gson();
            OutputStream outputStream = new FileOutputStream(FileSystemHelper.getUserOutFolderWithFilename(mailId, user, META));
            InputStream userPublicKeyStream = new FileInputStream(userService.getPubKeyFile());
            Set<PGPPublicKey> publicKeys = new HashSet<PGPPublicKey>();
            publicKeys.add(readPublicKey(userPublicKeyStream));

            encrypt(outputStream, gson.toJson(new OutBoxMeta(new Date().getTime(), recipients, mailId)).getBytes(StandardCharsets.UTF_8), publicKeys);
            SignedFileProcessor.signFile(
                    FileSystemHelper.getUserOutFolderWithFilename(mailId, user, META),
                    new FileInputStream(BC_STORAGE_KEYS_KEY_SKR),
                    new FileOutputStream(FileSystemHelper.getUserOutFolderWithFilename(mailId, user, META)),
                    "".toCharArray(),
                    true
            );

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
                FileSystemHelper.copyFileUsingStream(pubKey.getResource().getFile(), dest);

                dest = new File(FileSystemHelper.getUserInFolderByName(address, privKey.getOriginalFilename()));
                FileSystemHelper.copyFileUsingStream(privKey.getResource().getFile(), dest);

                return ResponseEntity.ok(new Response(HttpStatus.OK));
            }
        }
        return ResponseEntity.ok(new Response(HttpStatus.FORBIDDEN));
    }

    private void storeTempFilesForOutbound(List<OutgoingFiles> files, String mailId) throws IOException, NoSuchAlgorithmException {
        FileSystemHelper.createFolder(FileSystemHelper.getTempFolderForOutboundMail(mailId));
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
        FileSystemHelper.createFolder(FileSystemHelper.getTempFolderForIncomingMail(mailHeader));
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
                deployMetaFiles(mailHeader);
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            purgeTempFiles(mailHeader);
        }
    }

    private void storeOutgoingFile(List<OutgoingFiles> files, String mailId, String user) throws Exception {
        FileSystemHelper.createFolder(FileSystemHelper.getUserOutFolder(mailId, user));

        for (OutgoingFiles file : files) {

            File dest = new File(FileSystemHelper.getUserOutFolderWithFilename(mailId, user, file.getFile().getOriginalFilename()));
            FileSystemHelper.copyFileUsingStream(file.getTempPath(), dest);
        }
    }

    private void purgeOutgoingFiles(String mailId) {
        deployMetaFile();
        purgeOutboundTempFiles(mailId);
    }

    private void deployFiles(List<IncomingFiles> files, NewMail mailHeader) throws Exception {
        FileSystemHelper.createFolder(FileSystemHelper.getUserInFolderWithFilename(mailHeader));

        for (IncomingFiles file : files) {

            File dest = new File(FileSystemHelper.getUserInFolderWithFilename(mailHeader, file.getFile().getOriginalFilename()));
            FileSystemHelper.copyFileUsingStream(file.getTempPath(), dest);
        }
        deployMetaFile();
    }

    private void deployMetaFiles(NewMail mailHeader) throws Exception {
        Gson gson = new Gson();
        String outputFilename = FileSystemHelper.getUserInFolderWithFilename(mailHeader, META);

        OutputStream outputStream = new FileOutputStream(outputFilename);
        InputStream userPublicKeyStream = new FileInputStream(FileSystemHelper.getUserInFolderByName(mailHeader.getRecipientAddress(), PUB_ASC));

        Set<PGPPublicKey> publicKeys = new HashSet<PGPPublicKey>();
        publicKeys.add(readPublicKey(userPublicKeyStream));

        encrypt(outputStream, gson.toJson(new InBoxMeta(new Date().getTime(), mailHeader.getRecipientAddress(), mailHeader.getMailId())).getBytes(StandardCharsets.UTF_8), publicKeys);
        SignedFileProcessor.signFile(
                outputFilename,
                new FileInputStream(BC_STORAGE_KEYS_KEY_SKR),
                new FileOutputStream(outputFilename),
                "".toCharArray(),
                true
        );
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