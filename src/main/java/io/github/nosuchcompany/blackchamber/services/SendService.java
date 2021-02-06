package io.github.nosuchcompany.blackchamber.services;

import io.github.nosuchcompany.blackchamber.objects.mailobjects.NewMail;
import io.github.nosuchcompany.blackchamber.objects.mailobjects.OutgoingFiles;
import io.github.nosuchcompany.blackchamber.helper.ProtocolHelper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.nosuchcompany.blackchamber.helper.AlternateHostHelper.getFinalDestinationHost;

public class SendService {

    private final NewMail mail;
    private final List<OutgoingFiles> files;

    public SendService(NewMail mail, List<OutgoingFiles> files) {
        this.mail = mail;
        this.files = files;
    }

    public ResponseEntity<Void> send() throws MalformedURLException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        for (OutgoingFiles file : files) {
            body.add("attachments", new FileSystemResource(file.getTempPath().toPath()));
        }

        String url = ProtocolHelper.getProtocol(mail.getRecipientAddress().getHost()) + "{foreignMailBox}/in?sender={sender}&recipient={recipient}&mailId={mailId}";
        Map<String, String> params = new HashMap<String, String>();
        params.put("foreignMailBox", getFinalDestinationHost(mail.getRecipientAddress().getHost()));
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
