package com.maltabrainz.dovecote.services;

import com.maltabrainz.dovecote.generator.RSAGen;
import com.maltabrainz.dovecote.objects.UserInfo;

import java.io.*;

import static com.maltabrainz.dovecote.helper.FileSystemHelper.*;
import static com.maltabrainz.dovecote.helper.ShaHelper.getHash;
import static org.apache.tomcat.util.http.fileupload.FileUtils.deleteDirectory;

public class UserService {
    public static final String IN = "/in";
    public static final String OUT = "/out";
    private String pw;
    private String user;

    public UserService(String pw, String user) {
        this.pw = pw;
        this.user = user;
    }

    public boolean createUser() throws Exception {
        if (!createUserFolder(user)) {
            return false;
        }
        setupUserFolderAndMetaFiles();
        createKeys();
        return true;
    }

    public boolean validateUser() throws Exception {
        if (!folderExists(getUserFolder(user))) {
            return false;
        }
        return checkCredentials();
    }

    public void shredUser() throws Exception {
        deleteDirectory(new File(getUserFolder(user)));
    }

    public UserInfo getUserInformation() throws Exception {
        UserInfo ui = new UserInfo();
        ui.setInboxFilesAmount(String.valueOf(folderFileCount(new File(getUserFolder(user)))));
        ui.setOverallFileSize(String.valueOf(folderSize(new File(getUserFolder(user)))));
        ui.setOutboxFilesAmount(String.valueOf(folderFileCount(new File(getUserFolder(user) + OUT))));
        ui.setInboxFilesAmount(String.valueOf(folderFileCount(new File(getUserFolder(user) + IN))));

        return ui;
    }

    private void setupUserFolderAndMetaFiles() throws Exception {
        createFolder(getUserFolder(user) + IN);
        createFolder(getUserFolder(user) + OUT);
        createPWFile();
    }

    private void createPWFile() throws Exception {
        PrintStream out = new PrintStream(new FileOutputStream(getUserFolder(user) + "/" + getHash(user) + ".pw"));
        out.print(getHash(user + pw));
    }

    private void createKeys() throws Exception {
        RSAGen.createKeyPair(getHash(pw), user);
    }

    private boolean checkCredentials() throws Exception {
        InputStream input = getClass().getResourceAsStream(getUserFolder(user) + "/" + getHash(user) + ".pw");
        return readFromInputStream(input).equals(getHash(user + pw));
    }

    private String readFromInputStream(InputStream inputStream) throws IOException {

        StringBuilder resultStringBuilder = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = br.readLine()) != null) {
            resultStringBuilder.append(line).append("\n");
        }

        return resultStringBuilder.toString();
    }
}