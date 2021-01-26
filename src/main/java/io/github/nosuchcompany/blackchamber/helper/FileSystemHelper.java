package io.github.nosuchcompany.blackchamber.helper;

import io.github.nosuchcompany.blackchamber.objects.mailobjects.Address;
import io.github.nosuchcompany.blackchamber.objects.mailobjects.NewMail;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileSystemHelper {

    private static final String DOVECOTE_TEMP = "bc_storage/temp/";
    private static final String DOVECOTE = "bc_storage/";
    private static final String IN = "/in/";
    private static final String OUT = "/out/";
    private static final String SEPARATOR = "/";

    public static String getUserInFolderWithFilename(NewMail mailHeader, String fileName) throws Exception {
        return DOVECOTE + ShaHelper.getHash(mailHeader.getRecipientAddress().getUser()) + IN + mailHeader.getMailId() + SEPARATOR + fileName;
    }

    public static String getUserInFolderByName(Address address, String filename) throws Exception {
        return DOVECOTE + ShaHelper.getHash(address.getUser()) + SEPARATOR + filename;
    }

    public static String getUserInFolderWithFilename(NewMail mailHeader) throws Exception {
        return DOVECOTE + ShaHelper.getHash(mailHeader.getRecipientAddress().getUser()) + IN + mailHeader.getMailId();
    }

    public static String getUserOutFolder(String mailId, String user) throws Exception {
        return DOVECOTE + ShaHelper.getHash(new Address(user).getUser()) + OUT + mailId;
    }

    public static String getUserOutFolderWithFilename(String mailId, String user, String fileName) throws Exception {
        return DOVECOTE + ShaHelper.getHash(new Address(user).getUser()) + OUT + mailId + SEPARATOR + fileName;
    }

    public static String getUserFolder(String username) throws Exception {
        return DOVECOTE + ShaHelper.getHash(username);
    }

    public static String getTempFolderForIncomingMail(NewMail mailHeader) {
        return DOVECOTE_TEMP + mailHeader.getMailId();
    }

    public static String getTempFolderForOutboundMail(String mailId) {
        return DOVECOTE_TEMP + mailId + "--OUT";
    }

    public static boolean createUserFolder(String username) throws Exception {
        File directory = new File(getUserFolder(username));
        if (directory.exists()) {
            return false;
        }
        directory.mkdir();
        return true;
    }

    public static void createFolder(String folder) {
        File directory = new File(folder);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    public static boolean folderExists(String folder) {
        File directory = new File(folder);
        return directory.exists();
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public static void moveDir(String src, String dest) throws IOException {
        Files.move(new File(src).toPath(), new File(dest).toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    public static long folderFileCount(File directory) {
        long count = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                count++;
            else
                count += folderFileCount(file);
        }
        return count;
    }

    public static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }
}
