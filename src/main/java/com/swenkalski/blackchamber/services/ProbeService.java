package com.swenkalski.blackchamber.services;

import com.swenkalski.blackchamber.objects.IncomingFiles;
import com.swenkalski.blackchamber.objects.NewMail;
import com.swenkalski.blackchamber.objects.Probe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.swenkalski.blackchamber.helper.ProtocolHelper.getProtocol;

public class ProbeService {

    public static final String IN_PROBE = ":1337/in/probe";
    public static final String HTTPS = "https://";

    private final List<IncomingFiles> files;
    private final NewMail mailHeader;

    public ProbeService(List<IncomingFiles> files, NewMail mailHeader) {
        this.files = files;
        this.mailHeader = mailHeader;
    }

    public boolean sendProbeToSenderServer() {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> map = new HashMap<>();
        map.put("mailId", mailHeader.getMailId());
        map.put("sender", mailHeader.getSender());
        map.put("recipient", mailHeader.getRecipient());

        List<String> hashes = new ArrayList<>();

        for (IncomingFiles file : files) {
            hashes.add(file.getHash());
        }

        map.put("attachments", hashes.toArray());
        ResponseEntity<Void> response = restTemplate.postForEntity(getProtocol(mailHeader.getSenderAddress().getHost()) +
                mailHeader.getSenderAddress().getHost() +
                IN_PROBE, map, Void.class);
        return response.getStatusCode() == HttpStatus.OK;
    }

    public boolean testProbeFromPossibleRecipient(Probe probe) {
        return true;
    }


}
