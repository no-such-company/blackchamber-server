package com.maltabrainz.dovecote.storage;

import com.maltabrainz.dovecote.helper.FileSystemHelper;
import com.maltabrainz.dovecote.objects.IncomingFiles;
import com.maltabrainz.dovecote.objects.NewMail;
import com.maltabrainz.dovecote.services.ProbeService;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static com.maltabrainz.dovecote.helper.ShaHelper.getFileChecksum;

public class StorageService {

    private List<IncomingFiles> files;
    private final NewMail mailHeader;

    public StorageService(List<IncomingFiles> files, NewMail mailHeader) {
        this.files = files;
        this.mailHeader = mailHeader;
    }

    public void storeFileTemp() throws Exception {
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

    public void deployFiles() throws Exception {
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
        return getFileChecksum(file);
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