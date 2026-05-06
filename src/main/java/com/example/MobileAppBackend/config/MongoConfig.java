package com.example.MobileAppBackend.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
@Configuration
public class MongoConfig {

    @Value("${MONGO_USERNAME}")
    private String username;

    @Value("${MONGO_PASSWORD}")
    private String password;

    @Value("${MONGO_URI}")
    private String host;

    @Value("${MONGO_TEST_URI:}")
    private String testHost;

    private final Environment environment;

    public MongoConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public MongoClient mongoClient() {


        String activeProfile = Arrays.toString(environment.getActiveProfiles());

        boolean isTest = activeProfile.contains("test");

        String selectedHost = isTest && !testHost.isEmpty() ? testHost : host;

        String encodedPassword =
                URLEncoder.encode(password, StandardCharsets.UTF_8);

        String dbName = isTest ? "recipe-test" : "recipe-prod";

        String uri = String.format(
                "mongodb+srv://%s:%s@%s/%s?retryWrites=true&w=majority&appName=Cluster0",
                username,
                encodedPassword,
                selectedHost,
                dbName
        );
        return MongoClients.create(uri);
    }
}