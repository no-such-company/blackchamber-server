package com.maltabrainz.dovecote.storage;

import com.maltabrainz.dovecote.helper.FileSystemHelper;
import com.maltabrainz.dovecote.objects.IncomingFiles;
import com.maltabrainz.dovecote.objects.NewMail;
import com.maltabrainz.dovecote.services.ProbeService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class StorageService {

    public static final String SHA_256 = "SHA-256";

    private List<IncomingFiles> files;
    private final NewMail mailHeader;

    public StorageService(List<IncomingFiles> files, NewMail mailHeader) {
        this.files = files;
        this.mailHeader = mailHeader;
    }

    public void storeFileTemp() throws IOException, NoSuchAlgorithmException {
        for (IncomingFiles file : files) {
            File dest = new File(FileSystemHelper.getTempFolderForIncomingMail(mailHeader));
            file.getFile().transferTo(dest);
            file.setHash(fetchHashOfFile(dest));
        }
        ProbeService probeService = new ProbeService(files, mailHeader);
        if (probeService.sendProbeToSenderServer()) {
            deployFiles();
        }

        purgeTempFiles();
    }

    public void deployFiles() throws IOException, NoSuchAlgorithmException {
        for (IncomingFiles file : files) {

            File dest = new File(FileSystemHelper.getUserInFolder(mailHeader, file.getFile().getOriginalFilename()));
            file.getFile().transferTo(dest);
            file.setHash(fetchHashOfFile(dest));
        }
        deployMetaFile();
    }

    public void purgeTempFiles() {
        deleteDirectory(new File(FileSystemHelper.getTempFolderForIncomingMail(mailHeader)));
    }

    private void deployMetaFile() {

    }

    private String fetchHashOfFile(File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest shaDigest = MessageDigest.getInstance(SHA_256);
        return getFileChecksum(shaDigest, file);
    }

    private String getFileChecksum(MessageDigest digest, File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        fis.close();
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}