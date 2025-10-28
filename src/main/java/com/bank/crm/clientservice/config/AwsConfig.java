package com.bank.crm.clientservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class AwsConfig {

    private static final Logger logger = LoggerFactory.getLogger(AwsConfig.class);

    @Bean
    public SqsClient sqsClient() {
        try {
            SqsClient client = SqsClient.builder()
                    .region(Region.AP_SOUTHEAST_1)
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
            logger.info("SqsClient initialized successfully for region: ap-southeast-1");
            return client;
        } catch (Exception e) {
            logger.error("Failed to initialize SqsClient: {}", e.getMessage(), e);
            throw e;
        }
    }
}
