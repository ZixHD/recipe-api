package com.example.MobileAppBackend.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class MongoConfig {

    @Value("${MONGO_USERNAME}")
    private String username;

    @Value("${MONGO_PASSWORD}")
    private String password;

    @Value("${MONGO_URI}")
    private String host;

    @Bean
    public MongoClient mongoClient() {

        String encodedPassword =
                URLEncoder.encode(password, StandardCharsets.UTF_8);

        String uri = String.format(
                "mongodb+srv://%s:%s@%s/?retryWrites=true&w=majority&appName=Cluster0",
                username,
                encodedPassword,
                host
        );

        log.info("Connecting to MongoDB Atlas...");

        return MongoClients.create(uri);
    }
}