package com.swenkalski.blackchamber.controller;

import com.google.gson.Gson;
import com.swenkalski.blackchamber.objects.mailobjects.Probe;
import com.swenkalski.blackchamber.objects.response.InformationResponse;
import com.swenkalski.blackchamber.objects.response.Response;
import com.swenkalski.blackchamber.services.ProbeService;
import com.swenkalski.blackchamber.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import com.swenkalski.blackchamber.objects.mailobjects.Address;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class BlackChamberController {

    @Value("${bc.version}")
    private String version;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<InformationResponse> welcome() {
        return ResponseEntity.ok(new InformationResponse(HttpStatus.OK, version));
    }

    @RequestMapping(value = "/in/probe", method = RequestMethod.POST)
    public ResponseEntity<String> probeSendMail(@RequestParam("attachments") String[] files
            , @RequestParam("sender") String sender
            , @RequestParam("recipient") String recipient
            , @RequestParam("mailId") String mailId) throws Exception {

        ProbeService probeService = new ProbeService(null, null);
        Probe probe = new Probe(mailId, sender, recipient, files);
        Gson gson = new Gson();
        return new ResponseEntity<String>(gson.toJson(probeService.testProbeFromPossibleRecipient(probe)), new HttpHeaders(), HttpStatus.OK);
    }

    @RequestMapping(value = "/inbox/create", method = RequestMethod.POST)
    public ResponseEntity<Response> createUser(@RequestParam("user") String user, @RequestParam("hash") String pwHash, @RequestParam("keyHash") String keyHash) throws Exception {
        UserService userService = new UserService(pwHash, keyHash, new Address(user));
        try {
            if (!userService.createUser()) {
                return ResponseEntity.ok(new Response(HttpStatus.BAD_REQUEST));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new Response(HttpStatus.INTERNAL_SERVER_ERROR));
        }
        return ResponseEntity.ok(new Response(HttpStatus.OK));
    }

    @RequestMapping(value = "/inbox/delete", method = RequestMethod.POST)
    public ResponseEntity deleteUser(@RequestParam("user") String user, @RequestParam("hash") String pwHash) throws Exception {
        UserService userService = new UserService(pwHash, new Address(user));
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
    public Object fetchInboxInformation(@RequestParam("user") String user, @RequestParam("hash") String pwHash) throws Exception {
        UserService userService = new UserService(pwHash, new Address(user));
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
    public Object fetchInboxPubKey(@RequestParam("user") String user, @RequestParam("hash") String pwHash) throws Exception {
        UserService userService = new UserService(pwHash, new Address(user));
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

    @RequestMapping(value = "/inbox/renewkeys", method = RequestMethod.POST)
    public Object renewInboxKeys(@RequestParam("user") String user,
                                 @RequestParam("hash") String pwHash,
                                 @RequestParam("keyHash") String keyHash) throws Exception {
        UserService userService = new UserService(pwHash, keyHash, new Address(user));
        try {
            if (!userService.renewKey()) {
                return ResponseEntity.ok(new Response(HttpStatus.BAD_REQUEST));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new Response(HttpStatus.INTERNAL_SERVER_ERROR));
        }
        return ResponseEntity.ok(new Response(HttpStatus.OK));
    }

    @RequestMapping(value = "/inbox/mails", method = RequestMethod.POST)
    public Object fetchInboxList(@RequestParam("user") String user, @RequestParam("hash") String pwHash) throws Exception {
        UserService userService = new UserService(pwHash, new Address(user));
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
    public Object fetchMailItemByID(@RequestParam("user") String user, @RequestParam("hash") String pwHash, @RequestParam("mailId") String mailId, @RequestParam("fileId") String fileId) throws Exception {
        UserService userService = new UserService(pwHash, new Address(user));
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
    public Object removeMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwHash, @RequestParam("mailId") String mailId) throws Exception {
        UserService userService = new UserService(pwHash, new Address(user));
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
    public Object moveMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwHash, @RequestParam("mailId") String mailId, @RequestParam("dest") String dest) throws Exception {
        UserService userService = new UserService(pwHash, new Address(user));
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
}