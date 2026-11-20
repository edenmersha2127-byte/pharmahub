package com.medicinelocator.auth;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class AuthApplication {

    public static void main(String[] args) {

        // Load .env from working directory into System properties BEFORE Spring starts
        // Look for .env in the working dir first, then the repo root (when run from a service folder).
        Dotenv.configure()
                .directory(java.nio.file.Files.exists(java.nio.file.Path.of(".env")) ? "." : "..")
                .ignoreIfMissing()
                .load()
                .entries()
                .forEach(e -> System.setProperty(e.getKey(), e.getValue()));

        SpringApplication.run(AuthApplication.class, args);
    }
}