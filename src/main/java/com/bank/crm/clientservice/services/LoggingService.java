package com.bank.crm.clientservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoggingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.logging-queue-url}")
    private String queueUrl;

    public LoggingService(SqsClient sqsClient, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    public void sendCreateLog(String agentId, String clientId, String remarks) {
        logger.info("Preparing to send CREATE log for clientId: {}, agentId: {}", clientId, agentId);
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("crud_operation", "Create");
        messageBody.put("attribute_name", "");
        messageBody.put("before_value", "");
        messageBody.put("after_value", "");
        messageBody.put("agent_id", agentId);
        messageBody.put("client_id", clientId);
        messageBody.put("date_time", LocalDateTime.now().format(ISO_FORMATTER));
        messageBody.put("remarks", remarks);

        sendMessage(messageBody);
    }

    public void sendReadLog(String agentId, String clientId, String remarks) {
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("crud_operation", "Read");
        messageBody.put("attribute_name", "");
        messageBody.put("before_value", "");
        messageBody.put("after_value", "");
        messageBody.put("agent_id", agentId);
        messageBody.put("client_id", clientId);
        messageBody.put("date_time", LocalDateTime.now().format(ISO_FORMATTER));
        messageBody.put("remarks", remarks);

        sendMessage(messageBody);
    }

    public void sendUpdateLog(String agentId, String clientId, String attributeName, String beforeValue, String afterValue, String remarks) {
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("crud_operation", "Update");
        messageBody.put("attribute_name", attributeName);
        messageBody.put("before_value", beforeValue != null ? beforeValue : "");
        messageBody.put("after_value", afterValue != null ? afterValue : "");
        messageBody.put("agent_id", agentId);
        messageBody.put("client_id", clientId);
        messageBody.put("date_time", LocalDateTime.now().format(ISO_FORMATTER));
        messageBody.put("remarks", remarks);

        sendMessage(messageBody);
    }

    public void sendDeleteLog(String agentId, String clientId, String remarks) {
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("crud_operation", "Delete");
        messageBody.put("attribute_name", "");
        messageBody.put("before_value", "");
        messageBody.put("after_value", "");
        messageBody.put("agent_id", agentId);
        messageBody.put("client_id", clientId);
        messageBody.put("date_time", LocalDateTime.now().format(ISO_FORMATTER));
        messageBody.put("remarks", remarks);

        sendMessage(messageBody);
    }

    private void sendMessage(Map<String, Object> messageBody) {
        try {
            logger.info("Attempting to send message to SQS queue: {}", queueUrl);
            logger.debug("Message body: {}", messageBody);

            String jsonMessage = objectMapper.writeValueAsString(messageBody);
            logger.debug("JSON message: {}", jsonMessage);

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(jsonMessage)
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
            logger.info("Successfully sent log message to SQS. MessageId: {}, Queue: {}",
                    response.messageId(), queueUrl);

        } catch (Exception e) {
            logger.error("Failed to send log message to SQS. Queue: {}, Error: {}",
                    queueUrl, e.getMessage(), e);
            // Print stack trace for debugging
            e.printStackTrace();
        }
    }
}
