package com.maltabrainz.dovecote.helper;

import com.maltabrainz.dovecote.objects.NewMail;

import java.io.File;
import java.security.NoSuchAlgorithmException;

import static com.maltabrainz.dovecote.helper.ShaHelper.getHash;

public class FileSystemHelper {

    private static final String DOVECOTE_TEMP = "dovecote/temp/";
    private static final String DOVECOTE = "dovecote/";
    private static final String IN = "/in/";
    private static final String SEPARATOR = "/";

    public static String getUserInFolder(NewMail mailHeader, String fileName) throws Exception {
        return DOVECOTE + getHash(mailHeader.getRecipient()) + IN + mailHeader.getMailHash() + SEPARATOR + fileName;
    }

    public static String getUserFolder(String username) throws Exception {
        return DOVECOTE + getHash(username);
    }

    public static String getTempFolderForIncomingMail(NewMail mailHeader) {
        return DOVECOTE_TEMP + mailHeader.getMailHash();
    }

    public static boolean createUserFolder(String username) throws Exception {
        File directory = new File(getUserFolder(username));
        if (directory.exists()){
            return false;
        }
        directory.mkdir();
        return true;
    }

    public static void createFolder(String folder){
        File directory = new File(folder);
        if (!directory.exists()){
            directory.mkdir();
        }
    }
}
