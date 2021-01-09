package com.swenkalski.blackchamber.services;

import com.swenkalski.blackchamber.objects.IncomingFiles;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendService {

    private final String sender;
    private final String recipient;
    private final String mailId;
    private final List<IncomingFiles> files;

    public SendService(String sender, String recipient, String mailId, List<IncomingFiles> files) {
        this.sender = sender;
        this.recipient = recipient;
        this.mailId = mailId;
        this.files = files;
    }

    public void send() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        for (IncomingFiles file : files) {
            body.add("attachments", file.getFile());
        }

        String url = "https://{foreignMailBox}:1337/in?sender={sender}&recipient={recipient}&mailid={mailId}";
        Map<String, String> params = new HashMap<String, String>();
        params.put("foreignMailBox", "1234");
        params.put("sender", sender);
        params.put("recipient", recipient);
        params.put("mailId", mailId);
        URI uri = UriComponentsBuilder.fromUriString(url)
                .buildAndExpand(params)
                .toUri();

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);


        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForEntity(uri, requestEntity, String.class);
    }
}
