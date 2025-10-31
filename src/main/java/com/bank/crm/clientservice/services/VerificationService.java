package com.bank.crm.clientservice.services;

import com.bank.crm.clientservice.repositories.ClientProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import com.bank.crm.clientservice.dto.VerificationRequest;
import com.bank.crm.clientservice.dto.VerificationResult;
import com.bank.crm.clientservice.models.ClientProfile;
import com.bank.crm.clientservice.models.enums.ClientStatusTypes;
import com.bank.crm.clientservice.exceptions.ClientNotFoundException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final ClientProfileRepository clientProfileRepository;
    private final LoggingService loggingService;

    @Value("${aws.sqs.verification_request_queue_url}")
    private String verificationRequestQueueUrl;

    // Common date formats to try
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

    /**
     * Send verification request to SQS
     */
    public void sendVerificationRequest(UUID clientId, String userId, String agentEmail, String clientEmail) {
        try {
            VerificationRequest request = VerificationRequest.builder()
                    .clientId(clientId.toString())
                    .clientEmail(clientEmail)
                    .agent_Id(userId)
                    .agentEmail(agentEmail)
                    .timestamp(java.time.Instant.now().toString())
                    .build();

            String jsonMessage = objectMapper.writeValueAsString(request);

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(verificationRequestQueueUrl)
                    .messageBody(jsonMessage)
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
            logger.info("Successfully sent verification request to SQS. MessageId: {}, ClientId: {}",
                    response.messageId(), clientId);

        } catch (Exception e) {
            logger.error("Failed to send verification request to SQS for clientId: {}. Error: {}",
                    clientId, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification request", e);
        }
    }

    /**
     * Process verification result from SQS
     */
    public void processVerificationResult(VerificationResult result) {
        try {
            UUID clientId = UUID.fromString(result.getClientId());
            logger.info("Processing verification result for clientId: {}", clientId);

            // Fetch client profile from database
            ClientProfile clientProfile = clientProfileRepository.findById(clientId)
                    .orElseThrow(() -> new ClientNotFoundException(clientId));

            // Perform verification checks
            boolean isVerified = performVerification(clientProfile, result);

            if (isVerified) {
                // Update client status to ACTIVE
                clientProfile.setStatus(ClientStatusTypes.ACTIVE);
                clientProfileRepository.save(clientProfile);

                String remarks = String.format(
                        "Auto-verification successful for client %s. Status updated to ACTIVE.",
                        clientId
                );
                loggingService.sendUpdateLog(
                        clientProfile.getAgent_id(),
                        clientId.toString(),
                        "Status",
                        "PENDING",
                        "ACTIVE",
                        remarks
                );

                logger.info("Client {} verification successful. Status updated to ACTIVE", clientId);
            } else {
                String remarks = String.format(
                        "Auto-verification failed for client %s. Manual verification required.",
                        clientId
                );
                loggingService.sendUpdateLog(
                        clientProfile.getAgent_id(),
                        clientId.toString(),
                        "Auto-Verification",
                        "In Progress",
                        "Failed",
                        remarks
                );

                logger.warn("Client {} verification failed. Manual verification required", clientId);
            }

        } catch (ClientNotFoundException e) {
            logger.error("Client not found for verification result: {}", result.getClientId(), e);
        } catch (Exception e) {
            logger.error("Error processing verification result for clientId: {}. Error: {}",
                    result.getClientId(), e.getMessage(), e);
        }
    }

    /**
     * Perform verification checks against client profile
     */
    private boolean performVerification(ClientProfile clientProfile, VerificationResult result) {
        Map<String, String> extractedData = result.getExtractedData().getKeyValuePairs();

        // Check 1: Verify Name (First Name and Last Name should be in the extracted "Name" field)
        boolean nameMatch = verifyName(clientProfile, extractedData);

        // Check 2: Verify Date of Birth
        boolean dobMatch = verifyDateOfBirth(clientProfile, extractedData);

        logger.info("Verification results for client {}: nameMatch={}, dobMatch={}",
                clientProfile.getClientId(), nameMatch, dobMatch);

        return nameMatch && dobMatch;
    }

    /**
     * Verify that first name and last name are present in the extracted Name field
     */
    private boolean verifyName(ClientProfile clientProfile, Map<String, String> extractedData) {
        String extractedName = extractedData.get("Name");

        if (extractedName == null || extractedName.trim().isEmpty()) {
            logger.warn("No name found in extracted data");
            return false;
        }

        // Normalize names for comparison (remove extra spaces, convert to uppercase)
        String normalizedExtractedName = extractedName.trim().toUpperCase().replaceAll("\\s+", " ");
        String normalizedFirstName = clientProfile.getFirstName().trim().toUpperCase();
        String normalizedLastName = clientProfile.getLastName().trim().toUpperCase();

        // Check if both first name and last name are contained in the extracted name
        boolean firstNamePresent = normalizedExtractedName.contains(normalizedFirstName);
        boolean lastNamePresent = normalizedExtractedName.contains(normalizedLastName);

        logger.debug("Name verification: Extracted='{}', FirstName='{}' (present: {}), LastName='{}' (present: {})",
                normalizedExtractedName, normalizedFirstName, firstNamePresent,
                normalizedLastName, lastNamePresent);

        return firstNamePresent && lastNamePresent;
    }

    /**
     * Verify date of birth with dynamic format handling
     */
    private boolean verifyDateOfBirth(ClientProfile clientProfile, Map<String, String> extractedData) {
        String extractedDob = extractedData.get("Date of Birth");

        if (extractedDob == null || extractedDob.trim().isEmpty()) {
            logger.warn("No date of birth found in extracted data");
            return false;
        }

        // Parse the extracted date using multiple formatters
        LocalDate parsedExtractedDate = parseDate(extractedDob.trim());

        if (parsedExtractedDate == null) {
            logger.warn("Failed to parse extracted date of birth: {}", extractedDob);
            return false;
        }

        // Compare with client profile date of birth
        boolean match = parsedExtractedDate.equals(clientProfile.getDateOfBirth());

        logger.debug("DOB verification: Extracted='{}' (parsed: {}), Profile='{}', Match={}",
                extractedDob, parsedExtractedDate, clientProfile.getDateOfBirth(), match);

        return match;
    }

    /**
     * Parse date string using multiple formatters
     */
    private LocalDate parseDate(String dateString) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        return null;
    }
}


