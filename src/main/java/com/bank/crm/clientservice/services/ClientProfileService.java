package com.bank.crm.clientservice.services;

import com.bank.crm.clientservice.dto.ClientProfileCreateRequest;
import com.bank.crm.clientservice.dto.ClientProfileUpdateRequest;
import com.bank.crm.clientservice.dto.ClientProfileResponse;
import com.bank.crm.clientservice.dto.ClientStatusResponse;
import com.bank.crm.clientservice.exceptions.ClientNotFoundException;
import com.bank.crm.clientservice.exceptions.ClientNotPendingException;
import com.bank.crm.clientservice.exceptions.NonUniqueFieldException;
import com.bank.crm.clientservice.models.ClientProfile;
import com.bank.crm.clientservice.models.enums.ClientStatusTypes;
import com.bank.crm.clientservice.models.enums.GenderTypes;
import com.bank.crm.clientservice.repositories.ClientProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ClientProfileService {
    private final ClientProfileRepository clientProfileRepository;
    private final LoggingService loggingService;

    public ClientProfileResponse createClientProfile( ClientProfileCreateRequest clientProfileCreateRequest, String userId) {
        validateEmailAndPhoneUniqueness(clientProfileCreateRequest);
        ClientProfile clientProfile = ClientProfile.builder()
                .firstName(clientProfileCreateRequest.getFirstName())
                .lastName(clientProfileCreateRequest.getLastName())
                .dateOfBirth(clientProfileCreateRequest.getDateOfBirth())
                .gender(GenderTypes.valueOf(clientProfileCreateRequest.getGender()))
                .emailAddress(clientProfileCreateRequest.getEmailAddress())
                .phoneNumber(clientProfileCreateRequest.getPhoneNumber())
                .address(clientProfileCreateRequest.getAddress())
                .city(clientProfileCreateRequest.getCity())
                .state(clientProfileCreateRequest.getState())
                .country(clientProfileCreateRequest.getCountry())
                .postalCode(clientProfileCreateRequest.getPostalCode())
                .status(ClientStatusTypes.PENDING)
                .build();

        clientProfile.setAgent_id(userId);

        ClientProfile saved = clientProfileRepository.save(clientProfile);

        //Create remarks for logging
        String remarks = String.format(
                "Client profile created: Name - %s %s, Email - %s, Phone - %s, Status - %s under Agent ID - %s",
                clientProfile.getFirstName(),
                clientProfile.getLastName(),
                clientProfile.getEmailAddress(),
                clientProfile.getPhoneNumber(),
                clientProfile.getStatus(),
                userId
        );

        loggingService.sendCreateLog(userId, saved.getClientId().toString(), remarks);

        return mapToClientProfileResponse(saved);
    }

    public void deleteClientProfile(UUID clientId, String userId) {
        var existingProfile = getClientProfile(clientId);
        existingProfile.setStatus(ClientStatusTypes.INACTIVE);

        String remarks = String.format(
                "Client profile with ID %s deleted by agent %s.",
                clientId,
                userId
        );

        loggingService.sendDeleteLog(userId, clientId.toString(), remarks);

        clientProfileRepository.save(existingProfile);
    }

    public ClientProfile getClientProfile(UUID clientId) {
        return clientProfileRepository.findById(clientId)
                .filter(profile -> profile.getStatus() != ClientStatusTypes.INACTIVE)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
    }

    public List<ClientProfile> getClientProfiles(String userId) {
        String remarks = String.format(
                "Batch retrieval of client profiles under Agent %s.",
                userId
        );

         List<ClientProfile> clientProfiles =  clientProfileRepository.findAll().stream()
                .filter(profile -> profile.getStatus() != ClientStatusTypes.INACTIVE)
                .filter(profile -> profile.getAgent_id().equals(userId))
                .toList();

         List <String> clientIds = new ArrayList<>();
            for (ClientProfile profile : clientProfiles) {
                clientIds.add(profile.getClientId().toString());
            }

        loggingService.sendReadLog(userId, clientIds.toString(), remarks);

        return clientProfiles;
    }

    public List<ClientProfile> getAllClientProfiles() {
        return clientProfileRepository.findAll().stream()
                .filter(profile -> profile.getStatus() != ClientStatusTypes.INACTIVE)
                .toList();
    }

    public ClientProfileResponse updateClientProfile(UUID clientId, ClientProfileUpdateRequest clientProfileUpdateRequest, String userId) {
        var existingProfile = getClientProfile(clientId);
        validateEmailAndPhoneUniqueness(clientId, clientProfileUpdateRequest);

        List<String> changedFields = new ArrayList<>();
        List<String> beforeValues = new ArrayList<>();
        List<String> afterValues = new ArrayList<>();

        if (clientProfileUpdateRequest.getFirstName() != null && !clientProfileUpdateRequest.getFirstName().equals(existingProfile.getFirstName())) {
            changedFields.add("First Name");
            beforeValues.add(existingProfile.getFirstName());
            afterValues.add(clientProfileUpdateRequest.getFirstName());
            existingProfile.setFirstName(clientProfileUpdateRequest.getFirstName());
        }

        if (clientProfileUpdateRequest.getLastName() != null && !clientProfileUpdateRequest.getLastName().equals(existingProfile.getLastName())) {
            changedFields.add("Last Name");
            beforeValues.add(existingProfile.getLastName());
            afterValues.add(clientProfileUpdateRequest.getLastName());
            existingProfile.setLastName(clientProfileUpdateRequest.getLastName());
        }

        if (clientProfileUpdateRequest.getDateOfBirth() != null && !clientProfileUpdateRequest.getDateOfBirth().equals(existingProfile.getDateOfBirth())) {
            changedFields.add("Date of Birth");
            beforeValues.add(existingProfile.getDateOfBirth().toString());
            afterValues.add(clientProfileUpdateRequest.getDateOfBirth().toString());
            existingProfile.setDateOfBirth(clientProfileUpdateRequest.getDateOfBirth());
        }

        if (clientProfileUpdateRequest.getGender() != null) {
            GenderTypes newGender = GenderTypes.fromString(clientProfileUpdateRequest.getGender());
            if (!newGender.equals(existingProfile.getGender())) {
                changedFields.add("Gender");
                beforeValues.add(existingProfile.getGender().toString());
                afterValues.add(newGender.toString());
                existingProfile.setGender(newGender);
            }
        }

        if (clientProfileUpdateRequest.getEmailAddress() != null && !clientProfileUpdateRequest.getEmailAddress().equals(existingProfile.getEmailAddress())) {
            changedFields.add("Email Address");
            beforeValues.add(existingProfile.getEmailAddress());
            afterValues.add(clientProfileUpdateRequest.getEmailAddress());
            existingProfile.setEmailAddress(clientProfileUpdateRequest.getEmailAddress());
        }

        if (clientProfileUpdateRequest.getPhoneNumber() != null && !clientProfileUpdateRequest.getPhoneNumber().equals(existingProfile.getPhoneNumber())) {
            changedFields.add("Phone Number");
            beforeValues.add(existingProfile.getPhoneNumber());
            afterValues.add(clientProfileUpdateRequest.getPhoneNumber());
            existingProfile.setPhoneNumber(clientProfileUpdateRequest.getPhoneNumber());
        }

        if (clientProfileUpdateRequest.getAddress() != null && !clientProfileUpdateRequest.getAddress().equals(existingProfile.getAddress())) {
            changedFields.add("Address");
            beforeValues.add(existingProfile.getAddress());
            afterValues.add(clientProfileUpdateRequest.getAddress());
            existingProfile.setAddress(clientProfileUpdateRequest.getAddress());
        }

        if (clientProfileUpdateRequest.getCity() != null && !clientProfileUpdateRequest.getCity().equals(existingProfile.getCity())) {
            changedFields.add("City");
            beforeValues.add(existingProfile.getCity());
            afterValues.add(clientProfileUpdateRequest.getCity());
            existingProfile.setCity(clientProfileUpdateRequest.getCity());
        }

        if (clientProfileUpdateRequest.getState() != null && !clientProfileUpdateRequest.getState().equals(existingProfile.getState())) {
            changedFields.add("State");
            beforeValues.add(existingProfile.getState());
            afterValues.add(clientProfileUpdateRequest.getState());
            existingProfile.setState(clientProfileUpdateRequest.getState());
        }

        if (clientProfileUpdateRequest.getCountry() != null && !clientProfileUpdateRequest.getCountry().equals(existingProfile.getCountry())) {
            changedFields.add("Country");
            beforeValues.add(existingProfile.getCountry());
            afterValues.add(clientProfileUpdateRequest.getCountry());
            existingProfile.setCountry(clientProfileUpdateRequest.getCountry());
        }

        if (clientProfileUpdateRequest.getPostalCode() != null && !clientProfileUpdateRequest.getPostalCode().equals(existingProfile.getPostalCode())) {
            changedFields.add("Postal Code");
            beforeValues.add(existingProfile.getPostalCode());
            afterValues.add(clientProfileUpdateRequest.getPostalCode());
            existingProfile.setPostalCode(clientProfileUpdateRequest.getPostalCode());
        }

        if (!changedFields.isEmpty()) {
            String fieldNames = String.join(" | ", changedFields);
            String beforeValue = String.join(" | ", beforeValues);
            String afterValue = String.join(" | ", afterValues);
            String remarks = String.format("Updated fields: %s", fieldNames);

            loggingService.sendUpdateLog(userId, clientId.toString(), fieldNames, beforeValue, afterValue, remarks);
        }

        ClientProfile updated = clientProfileRepository.save(existingProfile);
        return mapToClientProfileResponse(updated);
    }


    public ClientStatusResponse updateClientStatus(UUID clientId, boolean activate) {
        ClientProfile clientProfile = clientProfileRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        if (clientProfile.getStatus() != ClientStatusTypes.PENDING) {
            throw new ClientNotPendingException("Client status must be PENDING to verify");
        }

        clientProfile.setStatus(activate ? ClientStatusTypes.ACTIVE : ClientStatusTypes.INACTIVE);

        ClientProfile updated = clientProfileRepository.save(clientProfile);

        return new ClientStatusResponse(updated.getClientId(), updated.getStatus().name());
    }


    private ClientProfileResponse mapToClientProfileResponse(ClientProfile clientProfile) {
        return ClientProfileResponse.builder()
                .clientId(clientProfile.getClientId())
                .firstName(clientProfile.getFirstName())
                .lastName(clientProfile.getLastName())
                .dateOfBirth(clientProfile.getDateOfBirth())
                .gender(clientProfile.getGender())
                .emailAddress(clientProfile.getEmailAddress())
                .phoneNumber(clientProfile.getPhoneNumber())
                .address(clientProfile.getAddress())
                .city(clientProfile.getCity())
                .state(clientProfile.getState())
                .country(clientProfile.getCountry())
                .postalCode(clientProfile.getPostalCode())
                .build();
    }

    private void validateEmailAndPhoneUniqueness(UUID clientId, ClientProfileUpdateRequest request) {
        List<String> errors = Stream.of(
                request.getEmailAddress() != null && clientProfileRepository.existsByEmailAddressAndClientIdNot(request.getEmailAddress(), clientId)
                        ? "emailAddress" : null,
                request.getPhoneNumber() != null && clientProfileRepository.existsByPhoneNumberAndClientIdNot(request.getPhoneNumber(), clientId)
                        ? "phoneNumber" : null
        ).filter(Objects::nonNull).toList();

        if (!errors.isEmpty()) {
            throw new NonUniqueFieldException(errors.toArray(new String[0]));
        }
    }

    private void validateEmailAndPhoneUniqueness(ClientProfileCreateRequest clientProfileCreateRequest) {
        List<String> errors = Stream.of(
                clientProfileCreateRequest.getEmailAddress() != null && clientProfileRepository.existsByEmailAddress(clientProfileCreateRequest.getEmailAddress())
                        ? "emailAddress" : null,
                clientProfileCreateRequest.getPhoneNumber() != null && clientProfileRepository.existsByPhoneNumber(clientProfileCreateRequest.getPhoneNumber())
                        ? "phoneNumber" : null
        ).filter(Objects::nonNull).toList();

        if (!errors.isEmpty()) {
            throw new NonUniqueFieldException(errors.toArray(new String[0]));
        }
    }
}