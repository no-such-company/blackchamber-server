package io.github.nosuchcompany.blackchamber;

import io.github.nosuchcompany.blackchamber.helper.FileSystemHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static io.github.nosuchcompany.blackchamber.constants.Constants.*;

import static io.github.nosuchcompany.pgplug.utils.PGPUtils.generateKeyPair;


@ComponentScan
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        FileSystemHelper.createFolder(BC_STORAGE);

        FileSystemHelper.createFolder(BC_STORAGE_KEYS);
        FileSystemHelper.createFolder(BC_STORAGE_TEMP);
        try {
            if (!new File(BC_STORAGE_KEYS_PUB_ASC).exists() || !new File(BC_STORAGE_KEYS_KEY_SKR).exists()) {
                OutputStream privateOut = new FileOutputStream(BC_STORAGE_KEYS_KEY_SKR);
                OutputStream publicOut = new FileOutputStream(BC_STORAGE_KEYS_PUB_ASC);

                generateKeyPair(privateOut, publicOut, PASSWORD.toCharArray());

            }
        } catch (IOException e) {
            System.out.println(THERE_WAS_AN_ERROR_DURING_THE_KEY_CREATION_PROCESS_ABORT_STARTUP_BLACK_CHAMBER);
        }

        SpringApplication app = new SpringApplication(Application.class);
        app.run(args);
    }
}