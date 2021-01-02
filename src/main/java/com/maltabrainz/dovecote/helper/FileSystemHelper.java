package com.maltabrainz.dovecote.helper;

import com.maltabrainz.dovecote.objects.NewMail;

public class FileSystemHelper {

    private static final String DOVECOTE_TEMP = "/dovecote/temp/";
    private static final String DOVECOTE = "/dovecote/";
    private static final String IN = "/in/";
    private static final String SEPARATOR = "/";

    public static String getUserInFolder(NewMail mailHeader, String fileName) {
        return DOVECOTE + mailHeader.getRecipient() + IN + mailHeader.getMailHash() + SEPARATOR + fileName;
    }

    public static String getTempFolderForIncomingMail(NewMail mailHeader) {
        return DOVECOTE_TEMP + mailHeader.getMailHash();
    }
}
