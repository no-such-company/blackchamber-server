package com.maltabrainz.dovecote.services;

import com.maltabrainz.dovecote.objects.IncomingFiles;
import com.maltabrainz.dovecote.objects.NewMail;
import com.maltabrainz.dovecote.objects.Probe;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProbeService {

    public static final String IN_PROBE = ":3771/in/probe";
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
        ResponseEntity<Void> response = restTemplate.postForEntity(HTTPS +
                mailHeader.getSender().split("//")[0]+
                IN_PROBE, map, Void.class);
        return response.getStatusCode() == HttpStatus.OK;
    }

    public boolean testProbeFromPossibleRecipient(Probe probe){


        return true;
    }
}
