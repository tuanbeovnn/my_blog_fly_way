package com.myblogbackend.blog.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {
    private static final String FIREBASE_APP_NAME = "BLOG_NOTIFICATIONS";

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        try {
            return FirebaseApp.getInstance(FIREBASE_APP_NAME);
        } catch (IllegalStateException e) {
            return createFirebaseApp();
        }
    }

    private FirebaseApp createFirebaseApp() throws IOException {
        InputStream serviceAccount = new ClassPathResource("demo-firebase.json").getInputStream();
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        return FirebaseApp.initializeApp(options, FIREBASE_APP_NAME);
    }

    @Bean
    FirebaseMessaging firebaseMessaging(final FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
