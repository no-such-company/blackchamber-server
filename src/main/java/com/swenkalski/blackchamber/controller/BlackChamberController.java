package com.swenkalski.blackchamber.controller;

import com.swenkalski.blackchamber.objects.IncomingFiles;
import com.swenkalski.blackchamber.objects.NewMail;
import com.swenkalski.blackchamber.objects.Probe;
import com.swenkalski.blackchamber.services.ProbeService;
import com.swenkalski.blackchamber.services.UserService;
import com.swenkalski.blackchamber.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class BlackChamberController {

    @Autowired
    public BlackChamberController() {
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String welcome() {
        return "BlackChamber mailserver running";
    }

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

        StorageService storageService = new StorageService(attachments, mailHeader);

        try {
            storageService.storeFileTemp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/in/probe", method = RequestMethod.POST)
    public ResponseEntity probeSendMail(@RequestBody Probe probeModel) {

        ProbeService probeService = new ProbeService(null, null);
        if (!probeService.testProbeFromPossibleRecipient(probeModel)) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/inbox/create", method = RequestMethod.POST)
    public ResponseEntity createUser(@RequestParam("user") String user, @RequestParam("hash") String pwHash) {
        UserService userService = new UserService(user, pwHash);
        try {
            if (!userService.createUser()) {
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/inbox/delete", method = RequestMethod.POST)
    public ResponseEntity deleteUser(@RequestParam("user") String user, @RequestParam("hash") String pwHash) {
        UserService userService = new UserService(user, pwHash);
        try {
            if (userService.validateUser()) {
                userService.shredUser();
            } else {
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/inbox/info", method = RequestMethod.POST)
    public Object fetchInboxInformation(@RequestParam("user") String user, @RequestParam("hash") String pwHash) {
        UserService userService = new UserService(user, pwHash);
        try {
            if (userService.validateUser()) {
                return userService.getUserInformation();
            }
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    /*
    The Server will always return a pubkey to avoid, that the feature are misused as an confirmation to
    validate that a user exists. This should not happen.
     */
    @RequestMapping(value = "/in/pubkey", method = RequestMethod.POST)
    public Object fetchInboxPubKey(@RequestParam("user") String user) {
        UserService userService = new UserService(user, null);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(userService.getPubKeyFile().length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(userService.getPubKey());
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/inbox/privkey", method = RequestMethod.POST)
    public Object fetchInboxPubKey(@RequestParam("user") String user, @RequestParam("hash") String pwHash) {
        UserService userService = new UserService(user, pwHash);
        try {
            if (userService.validateUser()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
                headers.add("Pragma", "no-cache");
                headers.add("Expires", "0");

                return ResponseEntity.ok()
                        .headers(headers)
                        .contentLength(userService.getPrivateKeyFile().length())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(userService.getPrivateKey());
            }
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/inbox/setkeys", method = RequestMethod.POST)
    public String replaceInboxKeys(@RequestParam("user") String user,
                                   @RequestParam("hash") String pwhash,
                                   @RequestParam("pub") MultipartFile pubKey,
                                   @RequestParam("priv") MultipartFile privKey) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/renewkeys", method = RequestMethod.POST)
    public String renewInboxKeys(@RequestParam("user") String user,
                                 @RequestParam("hash") String pwhash,
                                 @RequestParam("pub") MultipartFile pubKey,
                                 @RequestParam("priv") MultipartFile privKey) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mails", method = RequestMethod.POST)
    public Object fetchInboxList(@RequestParam("user") String user, @RequestParam("hash") String pwHash) {
        UserService userService = new UserService(user, pwHash);
        try {
            if (userService.validateUser()) {
                return userService.getUserInformation();
            }
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/inbox/mail/file", method = RequestMethod.POST)
    public Object fetchMailItemByID(@RequestParam("user") String user, @RequestParam("hash") String pwHash, @RequestParam("mailId") String mailId, @RequestParam("fileId") String fileId) {
        UserService userService = new UserService(user, pwHash);
        try {
            if (userService.validateUser()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
                headers.add("Pragma", "no-cache");
                headers.add("Expires", "0");

                return ResponseEntity.ok()
                        .headers(headers)
                        .contentLength(userService.getFile(mailId, fileId).length())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(userService.getPrivateKey());
            }
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/inbox/mail/remove", method = RequestMethod.POST)
    public Object removeMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwHash, @RequestParam("mailId") String mailId) {
        UserService userService = new UserService(user, pwHash);
        try {
            if (userService.validateUser()) {
                userService.deleteMail(mailId);
            } else {
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/inbox/mail/move", method = RequestMethod.POST)
    public Object moveMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwHash, @RequestParam("mailId") String mailId, @RequestParam("dest") String dest) {
        UserService userService = new UserService(user, pwHash);
        try {
            if (userService.validateUser()) {
                userService.move(mailId, dest);
            } else {
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    /*
    Technical it is possible to send a Mail directly from Client BUT the probe of the origin would not work.
    So you must use the proper Endpoint.
     */
    @RequestMapping(value = "/inbox/mail/send", method = RequestMethod.POST)
    public String sendMail(@RequestParam("attachments") List<MultipartFile> files
            , @RequestParam("user") String user
            , @RequestParam("recipients") List<String> recipients
    ) {
        return "JSON";
    }
}