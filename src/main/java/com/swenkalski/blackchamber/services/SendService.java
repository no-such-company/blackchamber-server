package com.swenkalski.blackchamber.services;

import com.swenkalski.blackchamber.objects.mailobjects.IncomingFiles;
import com.swenkalski.blackchamber.objects.mailobjects.NewMail;
import com.swenkalski.blackchamber.objects.mailobjects.OutgoingFiles;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.swenkalski.blackchamber.helper.ProtocolHelper.getProtocol;

public class SendService {

    private final NewMail mail;
    private final List<OutgoingFiles> files;

    public SendService(NewMail mail, List<OutgoingFiles> files) {
        this.mail = mail;
        this.files = files;
    }

    public ResponseEntity<Void> send(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        for (OutgoingFiles file : files) {
            body.add("attachments", new FileSystemResource(file.getTempPath().toPath()));
        }

        String url = getProtocol(mail.getRecipientAddress().getHost()) + "{foreignMailBox}:1337/in?sender={sender}&recipient={recipient}&mailId={mailId}";
        Map<String, String> params = new HashMap<String, String>();
        params.put("foreignMailBox", mail.getRecipientAddress().getHost());
        params.put("sender", mail.getSender());
        params.put("recipient", mail.getRecipient());
        params.put("mailId", mail.getMailId());
        URI uri = UriComponentsBuilder.fromUriString(url)
                .buildAndExpand(params)
                .toUri();

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);


        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForEntity(uri, requestEntity, Void.class);
    }
}
