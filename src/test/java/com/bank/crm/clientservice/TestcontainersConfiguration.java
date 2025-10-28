package com.bank.crm.clientservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.0"));
    }

    @Bean
    @Primary
    public SqsClient sqsClient() {
        // Create a mock SqsClient for testing
        SqsClient mockSqsClient = mock(SqsClient.class);

        // Mock the sendMessage method to return a successful response
        SendMessageResponse mockResponse = SendMessageResponse.builder()
                .messageId("test-message-id")
                .build();

        // Use lenient stubbing to avoid strict stubbing issues
        when(mockSqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(mockResponse);

        // Mock close() method to do nothing
        org.mockito.Mockito.doNothing().when(mockSqsClient).close();

        return mockSqsClient;
    }
}
