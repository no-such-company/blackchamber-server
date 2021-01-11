package com.swenkalski.blackchamber.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.swenkalski.blackchamber.objects.mailobjects.IncomingFiles;
import com.swenkalski.blackchamber.objects.mailobjects.NewMail;
import com.swenkalski.blackchamber.objects.mailobjects.Probe;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.swenkalski.blackchamber.helper.FileSystemHelper.folderExists;
import static com.swenkalski.blackchamber.helper.FileSystemHelper.getUserFolder;
import static com.swenkalski.blackchamber.helper.ProtocolHelper.getProtocol;
import static com.swenkalski.blackchamber.helper.Sanitization.isHexHalfedSHA256;
import static com.swenkalski.blackchamber.helper.Sanitization.isHexSHA256;
import static com.swenkalski.blackchamber.helper.ShaHelper.getFileChecksum;

public class ProbeService {

    public static final String IN_PROBE = ":1337/in/probe";
    public static final String PARAMS = "?sender={sender}&recipient={recipient}&mailId={mailId}&attachments={attachments}";

    private final List<IncomingFiles> files;
    private final NewMail mailHeader;

    public ProbeService(List<IncomingFiles> files, NewMail mailHeader) {
        this.files = files;
        this.mailHeader = mailHeader;
    }

    public boolean sendProbeToSenderServer() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        Map<String, String> params = new HashMap<String, String>();
        params.put("mailId", mailHeader.getMailId());
        params.put("sender", mailHeader.getSender());
        params.put("recipient", mailHeader.getRecipient());

        List<String> hashes = new ArrayList<>();

        for (IncomingFiles file : files) {
            hashes.add(file.getHash().substring(0, 31));
        }
        params.put("attachments", String.join(",",hashes));

        URI uri = UriComponentsBuilder.fromUriString(getProtocol(mailHeader.getSenderAddress().getHost()) +
                mailHeader.getSenderAddress().getHost() +
                IN_PROBE + PARAMS)
                .buildAndExpand(params)
                .toUri();

        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(uri, requestEntity, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonObject responseObject = new Gson().fromJson(response.getBody().toString(), JsonObject.class);
            JsonArray attachments = responseObject.get("attachments").getAsJsonArray();

            List<String> originalHashes = new ArrayList<>();
            for (IncomingFiles file : files) {
                for (JsonElement hash : attachments) {
                    if (!isHexSHA256(hash.getAsString())) {
                        return false;
                    }
                    if (file.getHash().equals(hash.toString())) {
                        originalHashes.add(hash.toString());
                    }
                }
            }
            return attachments.size() == originalHashes.size();
        }

        return false;
    }

    public Probe testProbeFromPossibleRecipient(Probe probe) throws Exception {
        if (!isHexSHA256(probe.getMailId())) {
            return null;
        }
        if (!folderExists(getUserFolder(probe.getSender().split("//:")[1]) + "/out/" + probe.getMailId())) {
            return null;
        }
        File dir = new File(getUserFolder(probe.getSender().split("//:")[1]) + "/out/" + probe.getMailId());
        List<String> originalHashes = new ArrayList<>();
        for (File file : dir.listFiles()) {
            for (String hash : probe.getAttachments()) {
                if (!isHexHalfedSHA256(hash)) {
                    return null;
                }
                String fileHash = getFileChecksum(file);
                if (fileHash.contains(hash)) {
                    originalHashes.add(fileHash);
                }
            }
        }
        if (probe.getAttachments().length != originalHashes.size()) {
            return null;
        }
        probe.setAttachments(originalHashes.toArray(String[]::new));
        return probe;
    }
}
