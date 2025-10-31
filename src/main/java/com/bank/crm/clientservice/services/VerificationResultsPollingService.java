package com.bank.crm.clientservice.services;

import com.bank.crm.clientservice.dto.VerificationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class VerificationResultsPollingService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationResultsPollingService.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final VerificationService verificationService;

    @Value("${aws.sqs.verification_results_queue_url}")
    private String verificationResultsQueueUrl;

    @Value("${verification.polling.enabled:true}")
    private boolean pollingEnabled;

    @Value("${verification.polling.max-messages:10}")
    private int maxMessages;

    @Value("${verification.polling.wait-time-seconds:20}")
    private int waitTimeSeconds;

    private ExecutorService executorService;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    @PostConstruct
    public void startPolling() {
        if (!pollingEnabled) {
            logger.info("Verification results polling is disabled");
            return;
        }

        logger.info("Starting verification results polling service");
        isRunning.set(true);
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::pollMessages);
    }

    @PreDestroy
    public void stopPolling() {
        logger.info("Stopping verification results polling service");
        isRunning.set(false);
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private void pollMessages() {
        logger.info("Polling loop started for verification results queue: {}", verificationResultsQueueUrl);

        while (isRunning.get()) {
            try {
                ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                        .queueUrl(verificationResultsQueueUrl)
                        .maxNumberOfMessages(maxMessages)
                        .waitTimeSeconds(waitTimeSeconds)
                        .build();

                ReceiveMessageResponse receiveResponse = sqsClient.receiveMessage(receiveRequest);
                List<Message> messages = receiveResponse.messages();

                if (!messages.isEmpty()) {
                    logger.info("Received {} verification result message(s)", messages.size());
                }

                for (Message message : messages) {
                    processMessage(message);
                }

            } catch (Exception e) {
                logger.error("Error polling verification results queue: {}", e.getMessage(), e);
                // Sleep briefly before retrying
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.error("Polling thread interrupted", ie);
                    break;
                }
            }
        }

        logger.info("Polling loop stopped");
    }

    private void processMessage(Message message) {
        try {
            String messageBody = message.body();
            logger.debug("Processing message: {}", messageBody);

            // Parse the verification result
            VerificationResult result = objectMapper.readValue(messageBody, VerificationResult.class);

            // Process the verification result
            verificationService.processVerificationResult(result);

            // Delete the message from the queue after successful processing
            deleteMessage(message.receiptHandle());

            logger.info("Successfully processed and deleted verification result message for clientId: {}",
                    result.getClientId());

        } catch (Exception e) {
            logger.error("Error processing verification result message: {}. Message will remain in queue.",
                    e.getMessage(), e);
            // Message will be visible again after visibility timeout and can be reprocessed
        }
    }

    private void deleteMessage(String receiptHandle) {
        try {
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(verificationResultsQueueUrl)
                    .receiptHandle(receiptHandle)
                    .build();

            sqsClient.deleteMessage(deleteRequest);

        } catch (Exception e) {
            logger.error("Error deleting message from queue: {}", e.getMessage(), e);
        }
    }
}

