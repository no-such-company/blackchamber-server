package com.swenkalski.blackchamber;

import com.swenkalski.blackchamber.helper.FileSystemHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        FileSystemHelper.createFolder("dovecote");
        FileSystemHelper.createFolder("dovecote/temp");

        SpringApplication app = new SpringApplication(Application.class);
        app.run(args);
    }
}