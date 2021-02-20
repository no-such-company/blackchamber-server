package io.github.nosuchcompany.blackchamber.services;

import io.github.nosuchcompany.blackchamber.objects.mailobjects.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.nosuchcompany.blackchamber.helper.FileSystemHelper.*;
import static io.github.nosuchcompany.blackchamber.helper.ShaHelper.getHash;
import static io.github.nosuchcompany.pgplug.utils.PGPUtils.generateKeyPair;
import static org.apache.tomcat.util.http.fileupload.FileUtils.deleteDirectory;
import static io.github.nosuchcompany.blackchamber.constants.Constants.*;

public class UserService {

    private Environment env;

    private String pw;
    private Address user;
    private String keyPw;

    public UserService(Environment env, String pw, String keyPw, Address user) {
        this.env = env;
        this.pw = pw;
        this.user = user;
        this.keyPw = keyPw;
    }

    public UserService(Environment env, String pw, Address user) {
        this.env = env;
        this.pw = pw;
        this.user = user;
    }

    public boolean createUser() throws Exception {
        if (keyPw == null || keyPw.equals("") || keyPw.equals(pw)) {
            throw new Exception();
        }
        if (!createUserFolder(user.getUser())) {
            return false;
        }
        setupUserFolderAndMetaFiles();
        createKeys();
        return true;
    }

    public boolean renewKey() throws Exception {
        if (keyPw == null || keyPw.equals("") || keyPw.equals(pw)) {
            throw new Exception();
        }
        createKeys();
        return true;
    }

    public boolean validateUser() throws Exception {
        if (!folderExists(getUserFolder(user.getUser()))) {
            return false;
        }
        return checkCredentials();
    }

    public void shredUser() throws Exception {
        deleteDirectory(new File(getUserFolder(user.getUser())));
    }

    public MailBox getMailFolders() throws Exception {
        MailBox mailBox = new MailBox();
        List<MailFolder> folderList = new ArrayList<>();
        for (String folder : getUserFolders()) {
            List<Mails> mails = new ArrayList<>();
            for (String mailId : getMailIdsFromFolder(folder)) {
                mails.add(new Mails(mailId, getAttachmentFilesFromMailId(folder, mailId), getCommonFilesFromMailId(folder, mailId)));
            }
            folderList.add(new MailFolder(folder, mails));
        }
        mailBox.setFolder(folderList);
        return mailBox;
    }

    public UserInfo getUserInformation() throws Exception {
        UserInfo ui = new UserInfo();
        ui.setInboxFilesAmount(String.valueOf(folderFileCount(new File(getUserFolder(user.getUser())))));
        ui.setOverallFileSize(String.valueOf(folderSize(new File(getUserFolder(user.getUser())))));
        ui.setOutboxFilesAmount(String.valueOf(folderFileCount(new File(getUserFolder(user.getUser()) + OUT))));
        ui.setInboxFilesAmount(String.valueOf(folderFileCount(new File(getUserFolder(user.getUser()) + IN))));

        return ui;
    }

    public File getPubKeyFile() throws Exception {
        return new File(getUserFolder(user.getUser()) + SEPERATOR + PUB_ASC);
    }

    public File getPrivateKeyFile() throws Exception {
        return new File(getUserFolder(user.getUser()) + SEPERATOR + KEY_SKR);
    }

    public File getFile(String mailId, String fileId, String folder) throws Exception {
        return new File(getUserFolder(user.getUser()) + SEPERATOR+ folder + SEPERATOR + mailId + SEPERATOR + fileId);
    }

    public ByteArrayResource getPrivateKey() throws Exception {
        Path path = Paths.get(getPrivateKeyFile().getAbsolutePath());
        return new ByteArrayResource(Files.readAllBytes(path));
    }

    public ByteArrayResource getFileBytes(String mailId, String fileId, String folder) throws Exception {
        Path path = Paths.get(getUserFolder(user.getUser()) + SEPERATOR+ folder + SEPERATOR + mailId + SEPERATOR + fileId);
        return new ByteArrayResource(Files.readAllBytes(path));
    }

    public ByteArrayResource getPubKey() throws Exception {
        Path path = Paths.get(getPubKeyFile().getAbsolutePath());
        return new ByteArrayResource(Files.readAllBytes(path));
    }

    public void deleteMail(String mailId) throws Exception {
        deleteDirectory(new File(getUserFolder(user.getUser()) + getFolderFromMailId(mailId) + SEPERATOR + mailId));
    }

    public boolean isDspExceed() throws Exception {
        long folderSize = (folderSize(new File(getUserFolder(user.getUser())))/ 1024) / 1024;
        return Long.valueOf(env.getProperty("user.max.dsp")) <= folderSize;
    }

    private void setupUserFolderAndMetaFiles() throws Exception {
        createFolder(getUserFolder(user.getUser()) + IN);
        createFolder(getUserFolder(user.getUser()) + OUT);
        createPWFile();
    }

    private void createPWFile() throws Exception {
        PrintStream out = new PrintStream(new FileOutputStream(getUserFolder(user.getUser()) + SEPERATOR + getHash(user.getUser()) + PW));
        out.print(getHash(user.getUser() + pw));
    }

    private void createKeys() throws Exception {
        OutputStream privateOut = new FileOutputStream(getPrivateKeyFile().getAbsolutePath());
        OutputStream publicOut = new FileOutputStream(getPubKeyFile().getAbsolutePath());

        generateKeyPair(privateOut, publicOut, getHash(keyPw).toCharArray());
    }

    private boolean checkCredentials() throws Exception {
        return Files.readString(Path.of(getUserFolder(user.getUser()) + SEPERATOR + getHash(user.getUser()) + PW)).equals(getHash(user.getUser() + pw));
    }

    public void move(String mailId, String dest) throws Exception {
        if (!Arrays.stream(getUserFolders()).anyMatch(dest::contains)) {
            createFolder(getUserFolder(user.getUser()) + SEPERATOR + dest);
        }
        moveDir(getUserFolder(user.getUser()) + SEPERATOR + getFolderFromMailId(mailId), getUserFolder(user.getUser()) + SEPERATOR + dest);
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
            if (new File(getUserFolder(user.getUser()) + SEPERATOR + folder + SEPERATOR + mailId).exists()) {
                return folder;
            }
        }
        return null;
    }

    private String[] getUserFolders() throws Exception {
        File file = new File(getUserFolder(user.getUser()));
        return file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
    }

    private String[] getMailIdsFromFolder(String folder) throws Exception {
        File file = new File(getUserFolder(user.getUser()) + SEPERATOR + folder);
        return file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
    }

    private String[] getCommonFilesFromMailId(String folder, String mailId) throws Exception {
        File file = new File(getUserFolder(user.getUser()) + SEPERATOR + folder + SEPERATOR + mailId);
        return file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isFile();
            }
        });
    }

    private String[] getAttachmentFilesFromMailId(String folder, String mailId) throws Exception {
        File file = new File(getUserFolder(user.getUser()) + SEPERATOR + folder + SEPERATOR + mailId + SEPERATOR + ATTACHMENT);
        return file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isFile();
            }
        });
    }
}