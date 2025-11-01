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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Value("${verification.polling.health-check-interval-seconds:60}")
    private int healthCheckIntervalSeconds;

    @Value("${verification.polling.max-restart-attempts:5}")
    private int maxRestartAttempts;

    @Value("${verification.polling.restart-delay-seconds:10}")
    private int restartDelaySeconds;

    private ExecutorService executorService;
    private ScheduledExecutorService healthCheckExecutor;
    private Future<?> pollingFuture;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicInteger restartCount = new AtomicInteger(0);
    private volatile long lastSuccessfulPollTime = 0;

    @PostConstruct
    public void startPolling() {
        if (!pollingEnabled) {
            logger.info("Verification results polling is disabled");
            return;
        }

        logger.info("Starting verification results polling service");
        isRunning.set(true);
        lastSuccessfulPollTime = System.currentTimeMillis();

        // Start the polling thread
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "verification-results-polling");
            thread.setDaemon(false);
            return thread;
        });
        pollingFuture = executorService.submit(this::pollMessages);

        // Start health check monitoring
        healthCheckExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "verification-polling-health-check");
            thread.setDaemon(true);
            return thread;
        });
        healthCheckExecutor.scheduleAtFixedRate(
                this::checkPollingHealth,
                healthCheckIntervalSeconds,
                healthCheckIntervalSeconds,
                TimeUnit.SECONDS
        );

        logger.info("Verification results polling service started with health check monitoring");
    }

    @PreDestroy
    public void stopPolling() {
        logger.info("Stopping verification results polling service");
        isRunning.set(false);

        // Stop health check executor
        if (healthCheckExecutor != null) {
            healthCheckExecutor.shutdown();
            try {
                if (!healthCheckExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    healthCheckExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                healthCheckExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Stop polling executor
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        logger.info("Verification results polling service stopped");
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

                // Update last successful poll time
                lastSuccessfulPollTime = System.currentTimeMillis();

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

    /**
     * Health check method that monitors the polling thread and restarts it if needed
     */
    private void checkPollingHealth() {
        try {
            // Check if the polling future is done (which means the thread has died unexpectedly)
            if (pollingFuture != null && pollingFuture.isDone() && isRunning.get()) {
                logger.error("Polling thread has died unexpectedly. Attempting to restart...");
                restartPolling();
                return;
            }

            // Check if the last successful poll was too long ago
            long timeSinceLastPoll = System.currentTimeMillis() - lastSuccessfulPollTime;
            long maxIdleTime = (waitTimeSeconds + 30) * 1000L; // Wait time + 30 seconds buffer

            if (timeSinceLastPoll > maxIdleTime) {
                logger.warn("No successful poll for {} seconds. Polling thread may be stuck. Attempting restart...",
                        timeSinceLastPoll / 1000);
                restartPolling();
            } else {
                logger.debug("Polling health check: OK (last poll {} seconds ago)", timeSinceLastPoll / 1000);
            }

        } catch (Exception e) {
            logger.error("Error during health check: {}", e.getMessage(), e);
        }
    }

    /**
     * Restart the polling thread with exponential backoff
     */
    private synchronized void restartPolling() {
        if (!isRunning.get()) {
            logger.info("Service is shutting down, skipping restart");
            return;
        }

        int currentRestartCount = restartCount.incrementAndGet();

        if (currentRestartCount > maxRestartAttempts) {
            logger.error("Max restart attempts ({}) reached. Polling service will not be restarted. Manual intervention required.",
                    maxRestartAttempts);
            isRunning.set(false);
            return;
        }

        try {
            logger.info("Attempting restart #{} of polling service", currentRestartCount);

            // Stop the existing executor
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdownNow();
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            }

            // Wait before restarting (exponential backoff)
            int delaySeconds = restartDelaySeconds * currentRestartCount;
            logger.info("Waiting {} seconds before restart...", delaySeconds);
            Thread.sleep(delaySeconds * 1000L);

            // Create a new executor and start polling
            executorService = Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(r, "verification-results-polling-" + currentRestartCount);
                thread.setDaemon(false);
                return thread;
            });

            lastSuccessfulPollTime = System.currentTimeMillis();
            pollingFuture = executorService.submit(this::pollMessages);

            logger.info("Polling service successfully restarted (attempt #{})", currentRestartCount);

            // Reset restart count after successful restart
            restartCount.set(0);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Restart interrupted", e);
        } catch (Exception e) {
            logger.error("Failed to restart polling service: {}", e.getMessage(), e);
        }
    }
}

