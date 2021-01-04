package com.maltabrainz.dovecote.controller;

import com.maltabrainz.dovecote.objects.IncomingFiles;
import com.maltabrainz.dovecote.objects.NewMail;
import com.maltabrainz.dovecote.objects.Probe;
import com.maltabrainz.dovecote.objects.UserInfo;
import com.maltabrainz.dovecote.services.ProbeService;
import com.maltabrainz.dovecote.services.UserService;
import com.maltabrainz.dovecote.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class DovecoteController {

    @Autowired
    public DovecoteController() {
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

    @RequestMapping(value = "/in/pubkey", method = RequestMethod.POST)
    public String fetchInboxPubKey(@RequestParam("user") String user) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/privkey", method = RequestMethod.POST)
    public String fetchInboxPubKey(@RequestParam("user") String user, @RequestParam("hash") String pwhash) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/setkeys", method = RequestMethod.POST)
    public String fetchInboxPubKey(@RequestParam("user") String user,
                                   @RequestParam("hash") String pwhash,
                                   @RequestParam("pub") MultipartFile pubKey,
                                   @RequestParam("priv") MultipartFile privKey) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mails", method = RequestMethod.POST)
    public String fetchInboxList(@RequestParam("user") String user, @RequestParam("hash") String pwhash) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mail", method = RequestMethod.POST)
    public String fetchMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwhash, @RequestParam("mailId") String mailId) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mail/file", method = RequestMethod.POST)
    public String fetchMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwhash, @RequestParam("mailId") String mailId, @RequestParam("fileId") String fileId) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mail/remove", method = RequestMethod.POST)
    public String removeMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwhash, @RequestParam("mailId") String mailId) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mail/move", method = RequestMethod.POST)
    public String moveMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwhash, @RequestParam("mailId") String mailId) {
        return "JSON";
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