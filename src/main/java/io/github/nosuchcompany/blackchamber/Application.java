package io.github.nosuchcompany.blackchamber;

import io.github.nosuchcompany.blackchamber.helper.FileSystemHelper;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.pgpainless.PGPainless;
import org.pgpainless.key.generation.type.rsa.RsaLength;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;

import static org.pgpainless.key.util.KeyRingUtils.getPrimaryPublicKeyFrom;

@ComponentScan
@SpringBootApplication
public class Application {

    public static final String LOCALHOST_DEFAULT = "localhost://default";
    public static final String BC_STORAGE = "bc_storage";
    public static final String BC_STORAGE_KEYS = "bc_storage/keys";
    public static final String BC_STORAGE_TEMP = "bc_storage/temp";
    public static final String BC_STORAGE_KEYS_PUB_ASC = "bc_storage/keys/pub.asc";
    public static final String BC_STORAGE_KEYS_KEY_SKR = "bc_storage/keys/key.skr";
    public static final String PASSWORD = ""; // we leave it without *may be changed in the future

    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, PGPException, IOException {
        FileSystemHelper.createFolder(BC_STORAGE);

        FileSystemHelper.createFolder(BC_STORAGE_KEYS);
        FileSystemHelper.createFolder(BC_STORAGE_TEMP);

        if(!new File(BC_STORAGE_KEYS_PUB_ASC).exists() || !new File(BC_STORAGE_KEYS_KEY_SKR).exists()  ){
            PGPSecretKeyRing keyRing = PGPainless.generateKeyRing().simpleRsaKeyRing(LOCALHOST_DEFAULT, RsaLength._8192, PASSWORD);

            FileUtils.writeByteArrayToFile(new File(BC_STORAGE_KEYS_PUB_ASC), getPrimaryPublicKeyFrom(keyRing).getEncoded());
            FileUtils.writeByteArrayToFile(new File(BC_STORAGE_KEYS_KEY_SKR), keyRing.getSecretKey().getEncoded());
        }

        SpringApplication app = new SpringApplication(Application.class);
        app.run(args);
    }
}