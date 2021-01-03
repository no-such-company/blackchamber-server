package com.maltabrainz.dovecote.services;

import com.maltabrainz.dovecote.generator.RSAGen;

import java.io.FileOutputStream;
import java.io.PrintStream;

import static com.maltabrainz.dovecote.helper.FileSystemHelper.*;
import static com.maltabrainz.dovecote.helper.ShaHelper.getHash;

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
}
