package com.swenkalski.blackchamber.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.swenkalski.blackchamber.objects.mailobjects.IncomingFiles;
import com.swenkalski.blackchamber.objects.mailobjects.NewMail;
import com.swenkalski.blackchamber.objects.mailobjects.Probe;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
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
            hashes.add(file.getHash().substring(0, 31));
        }

        map.put("attachments", hashes.toArray());
        ResponseEntity<Void> response = restTemplate.postForEntity(getProtocol(mailHeader.getSenderAddress().getHost()) +
                mailHeader.getSenderAddress().getHost() +
                IN_PROBE, map, Void.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonObject responseObject = new Gson().fromJson(response.getBody().toString(), JsonObject.class);
            JsonArray attachments = responseObject.get("attachments").getAsJsonArray();

            List<String> originalHashes = new ArrayList<>();
            for (IncomingFiles file : files) {
                for (JsonElement hash : attachments) {
                    if (!isHexSHA256(hash.toString())) {
                        return false;
                    }
                    if (file.getHash().equals(hash.toString())) {
                        originalHashes.add(hash.toString());
                    }
                }
            }
            return attachments.size() != originalHashes.size();
        }

        return false;
    }

    public Probe testProbeFromPossibleRecipient(Probe probe) throws Exception {
        if (!isHexSHA256(probe.getMailId())) {
            return null;
        }
        if (folderExists(getUserFolder(probe.getSender() + "/out/" + probe.getMailId()))) {
            return null;
        }
        File dir = new File(probe.getSender() + "/out/" + probe.getMailId());
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
