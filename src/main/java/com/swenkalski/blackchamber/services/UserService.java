package com.swenkalski.blackchamber.services;

import com.swenkalski.blackchamber.generator.RSAGen;
import com.swenkalski.blackchamber.objects.MailBox;
import com.swenkalski.blackchamber.objects.MailFolder;
import com.swenkalski.blackchamber.objects.UserInfo;
import org.springframework.core.io.ByteArrayResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.swenkalski.blackchamber.helper.FileSystemHelper.*;
import static com.swenkalski.blackchamber.helper.ShaHelper.getHash;
import static org.apache.tomcat.util.http.fileupload.FileUtils.deleteDirectory;

public class UserService {
    public static final String IN = "/in";
    public static final String OUT = "/out";
    public static final String SEPERATOR = "/";
    public static final String PUB_ASC = "/pub.asc";
    public static final String KEY_SKR = "/key.skr";
    public static final String PW = ".pw";
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

    public MailBox getMailFolders() throws Exception {
        MailBox mailBox = new MailBox();
        List<MailFolder> folderList = new ArrayList<>();
        for (String folder: getUserFolders()) {
            folderList.add(new MailFolder(getMailIdsFromFolder(folder),folder));
        }
        mailBox.setFolder(folderList);
        return mailBox;
    }

    public UserInfo getUserInformation() throws Exception {
        UserInfo ui = new UserInfo();
        ui.setInboxFilesAmount(String.valueOf(folderFileCount(new File(getUserFolder(user)))));
        ui.setOverallFileSize(String.valueOf(folderSize(new File(getUserFolder(user)))));
        ui.setOutboxFilesAmount(String.valueOf(folderFileCount(new File(getUserFolder(user) + OUT))));
        ui.setInboxFilesAmount(String.valueOf(folderFileCount(new File(getUserFolder(user) + IN))));

        return ui;
    }

    public File getPubKeyFile() throws Exception {
        return new File(getUserFolder(user) + PUB_ASC);
    }

    public File getPrivateKeyFile() throws Exception {
        return new File(getUserFolder(user) + KEY_SKR);
    }

    public File getFile(String mailId, String fileId) throws Exception {
        return new File(getUserFolder(user) + getFolderFromMailId(mailId) + SEPERATOR + mailId + SEPERATOR + fileId);
    }

    public ByteArrayResource getPrivateKey() throws Exception {
        Path path = Paths.get(getPrivateKeyFile().getAbsolutePath());
        return new ByteArrayResource(Files.readAllBytes(path));
    }

    public ByteArrayResource getPubKey() throws Exception {
        Path path = Paths.get(getPubKeyFile().getAbsolutePath());
        return new ByteArrayResource(Files.readAllBytes(path));
    }

    public void deleteMail(String mailId) throws Exception {
        deleteDirectory(new File(getUserFolder(user) + getFolderFromMailId(mailId) + SEPERATOR + mailId));
    }

    private void setupUserFolderAndMetaFiles() throws Exception {
        createFolder(getUserFolder(user) + IN);
        createFolder(getUserFolder(user) + OUT);
        createPWFile();
    }

    private void createPWFile() throws Exception {
        PrintStream out = new PrintStream(new FileOutputStream(getUserFolder(user) + SEPERATOR + getHash(user) + PW));
        out.print(getHash(user + pw));
    }

    private void createKeys() throws Exception {
        RSAGen.createKeyPair(getHash(pw), user);
    }

    private boolean checkCredentials() throws Exception {
        InputStream input = getClass().getResourceAsStream(getUserFolder(user) + SEPERATOR + getHash(user) + PW);
        return readFromInputStream(input).equals(getHash(user + pw));
    }

    public void move(String mailId, String dest) throws Exception {
        if (!Arrays.stream(getUserFolders()).anyMatch(dest::contains)) {
            createFolder(getUserFolder(user) + SEPERATOR + dest);
        }
        moveDir(getUserFolder(user) + SEPERATOR + getFolderFromMailId(mailId), getUserFolder(user) + SEPERATOR + dest);
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

    private String getFolderFromMailId(String mailId) throws Exception {
        for (String folder : getUserFolders()) {
            if (new File(getUserFolder(user) + SEPERATOR + folder + SEPERATOR + mailId).exists()) {
                return folder;
            }
        }
        return null;
    }

    private String[] getUserFolders() throws Exception {
        File file = new File(getUserFolder(user));
        return file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
    }

    private String[] getMailIdsFromFolder(String folder) throws Exception {
        File file = new File(getUserFolder(user)+SEPERATOR+folder);
        return file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
    }
}