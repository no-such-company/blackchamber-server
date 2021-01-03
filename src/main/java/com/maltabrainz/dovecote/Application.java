package com.maltabrainz.dovecote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

import static com.maltabrainz.dovecote.helper.FileSystemHelper.createFolder;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        createFolder("dovecote");
        
        SpringApplication app = new SpringApplication(Application.class);
        app.setDefaultProperties(Collections
                .singletonMap("server.port", "1337"));
        app.run(args);
    }
}