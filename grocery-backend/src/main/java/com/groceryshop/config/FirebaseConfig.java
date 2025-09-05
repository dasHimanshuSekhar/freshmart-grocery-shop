package com.groceryshop.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            // For demo purposes, using a mock service account key
            // In production, you would use actual Firebase service account credentials
            String serviceAccountKey = """
                {
                  "type": "service_account",
                  "project_id": "grocery-shop-demo",
                  "private_key_id": "demo-key-id",
                  "private_key": "-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7VJTUt9Us8cKB\\nDemo Key Content - Replace with actual Firebase credentials\\n-----END PRIVATE KEY-----\\n",
                  "client_email": "firebase-adminsdk-demo@grocery-shop-demo.iam.gserviceaccount.com",
                  "client_id": "123456789012345678901",
                  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                  "token_uri": "https://oauth2.googleapis.com/token"
                }
                """;

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(
                                new ByteArrayInputStream(serviceAccountKey.getBytes())))
                        .setDatabaseUrl("https://grocery-shop-demo-default-rtdb.firebaseio.com")
                        .build();

                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            // For demo purposes, we'll continue without Firebase initialization
            System.out.println("Firebase initialization skipped for demo - using mock data");
        }
    }
}
