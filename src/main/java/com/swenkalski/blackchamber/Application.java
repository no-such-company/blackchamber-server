package com.swenkalski.blackchamber;

import com.swenkalski.blackchamber.helper.FileSystemHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        FileSystemHelper.createFolder("bc_storage");
        FileSystemHelper.createFolder("bc_storage/temp");

        SpringApplication app = new SpringApplication(Application.class);
        app.run(args);
    }
}