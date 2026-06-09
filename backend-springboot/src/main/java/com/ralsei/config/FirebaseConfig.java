package com.ralsei.config;

import java.io.FileInputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-key:firebase-service-account.json}")
    private String serviceAccountKeyPath;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().stream().anyMatch(app -> app.getName().equals(FirebaseApp.DEFAULT_APP_NAME))) {
                return;
            }

            InputStream serviceAccount;
            try {
                serviceAccount = new ClassPathResource(serviceAccountKeyPath).getInputStream();
            } catch (Exception e) {
                serviceAccount = new FileInputStream(serviceAccountKeyPath);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized successfully");
        } catch (Exception e) {
            log.warn("Firebase Admin SDK could not be initialized: {}. Firebase token verification will be disabled.", e.getMessage());
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        if (FirebaseApp.getApps().stream().anyMatch(app -> app.getName().equals(FirebaseApp.DEFAULT_APP_NAME))) {
            return FirebaseAuth.getInstance();
        }
        return null;
    }
}