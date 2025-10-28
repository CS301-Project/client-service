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
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<ClientProfile> getClientProfiles(List<UUID> clientIds, String userId) {
        String remarks = String.format(
                "Batch retrieval of client profiles for IDs: %s by agent %s.",
                clientIds,
                userId
        );

        loggingService.sendReadLog(userId, clientIds.toString(), remarks);

        return clientIds.stream()
                .map(this::getClientProfile)
                .filter(profile -> profile.getStatus() != ClientStatusTypes.INACTIVE)
                .toList();
    }

    public List<ClientProfile> getAllClientProfiles() {
        return clientProfileRepository.findAll().stream()
                .filter(profile -> profile.getStatus() != ClientStatusTypes.INACTIVE)
                .toList();
    }

    public ClientProfileResponse updateClientProfile(UUID clientId, ClientProfileUpdateRequest clientProfileUpdateRequest, String userId) {
        var existingProfile = getClientProfile(clientId);
        validateEmailAndPhoneUniqueness(clientId, clientProfileUpdateRequest);

        // Track and log each field change
        if (clientProfileUpdateRequest.getFirstName() != null && !clientProfileUpdateRequest.getFirstName().equals(existingProfile.getFirstName())) {
            String beforeValue = existingProfile.getFirstName();
            String afterValue = clientProfileUpdateRequest.getFirstName();
            existingProfile.setFirstName(afterValue);
            String remarks = String.format("First name changed from %s to %s.", beforeValue, afterValue);
            loggingService.sendUpdateLog(userId, clientId.toString(), "First Name", beforeValue, afterValue, remarks);
        }

        if (clientProfileUpdateRequest.getLastName() != null && !clientProfileUpdateRequest.getLastName().equals(existingProfile.getLastName())) {
            String beforeValue = existingProfile.getLastName();
            String afterValue = clientProfileUpdateRequest.getLastName();
            existingProfile.setLastName(afterValue);
            String remarks = String.format("Last name changed from %s to %s.", beforeValue, afterValue);
            loggingService.sendUpdateLog(userId, clientId.toString(), "Last Name", beforeValue, afterValue, remarks);
        }

        if (clientProfileUpdateRequest.getDateOfBirth() != null && !clientProfileUpdateRequest.getDateOfBirth().equals(existingProfile.getDateOfBirth())) {
            String beforeValue = existingProfile.getDateOfBirth().toString();
            String afterValue = clientProfileUpdateRequest.getDateOfBirth().toString();
            existingProfile.setDateOfBirth(clientProfileUpdateRequest.getDateOfBirth());
            String remarks = String.format("Date of birth changed from %s to %s.", beforeValue, afterValue);
            loggingService.sendUpdateLog(userId, clientId.toString(), "Date of Birth", beforeValue, afterValue, remarks);
        }

        if (clientProfileUpdateRequest.getGender() != null) {
            GenderTypes newGender = GenderTypes.fromString(clientProfileUpdateRequest.getGender());
            if (!newGender.equals(existingProfile.getGender())) {
                String beforeValue = existingProfile.getGender().toString();
                String afterValue = newGender.toString();
                existingProfile.setGender(newGender);
                String remarks = String.format("Gender changed from %s to %s.", beforeValue, afterValue);
                loggingService.sendUpdateLog(userId, clientId.toString(), "Gender", beforeValue, afterValue, remarks);
            }
        }

        if (clientProfileUpdateRequest.getEmailAddress() != null && !clientProfileUpdateRequest.getEmailAddress().equals(existingProfile.getEmailAddress())) {
            String beforeValue = existingProfile.getEmailAddress();
            String afterValue = clientProfileUpdateRequest.getEmailAddress();
            existingProfile.setEmailAddress(afterValue);
            String remarks = String.format("Email address changed from %s to %s.", beforeValue, afterValue);
            loggingService.sendUpdateLog(userId, clientId.toString(), "Email Address", beforeValue, afterValue, remarks);
        }

        if (clientProfileUpdateRequest.getPhoneNumber() != null && !clientProfileUpdateRequest.getPhoneNumber().equals(existingProfile.getPhoneNumber())) {
            String beforeValue = existingProfile.getPhoneNumber();
            String afterValue = clientProfileUpdateRequest.getPhoneNumber();
            existingProfile.setPhoneNumber(afterValue);
            String remarks = String.format("Phone number changed from %s to %s.", beforeValue, afterValue);
            loggingService.sendUpdateLog(userId, clientId.toString(), "Phone Number", beforeValue, afterValue, remarks);
        }

        if (clientProfileUpdateRequest.getAddress() != null && !clientProfileUpdateRequest.getAddress().equals(existingProfile.getAddress())) {
            String beforeValue = existingProfile.getAddress();
            String afterValue = clientProfileUpdateRequest.getAddress();
            existingProfile.setAddress(afterValue);
            String remarks = String.format("Address changed from %s to %s.", beforeValue, afterValue);
            loggingService.sendUpdateLog(userId, clientId.toString(), "Address", beforeValue, afterValue, remarks);
        }

        if (clientProfileUpdateRequest.getCity() != null && !clientProfileUpdateRequest.getCity().equals(existingProfile.getCity())) {
            String beforeValue = existingProfile.getCity();
            String afterValue = clientProfileUpdateRequest.getCity();
            existingProfile.setCity(afterValue);
            String remarks = String.format("City changed from %s to %s.", beforeValue, afterValue);
            loggingService.sendUpdateLog(userId, clientId.toString(), "City", beforeValue, afterValue, remarks);
        }

        if (clientProfileUpdateRequest.getState() != null && !clientProfileUpdateRequest.getState().equals(existingProfile.getState())) {
            String beforeValue = existingProfile.getState();
            String afterValue = clientProfileUpdateRequest.getState();
            existingProfile.setState(afterValue);
            String remarks = String.format("State changed from %s to %s.", beforeValue, afterValue);
            loggingService.sendUpdateLog(userId, clientId.toString(), "State", beforeValue, afterValue, remarks);
        }

        if (clientProfileUpdateRequest.getCountry() != null && !clientProfileUpdateRequest.getCountry().equals(existingProfile.getCountry())) {
            String beforeValue = existingProfile.getCountry();
            String afterValue = clientProfileUpdateRequest.getCountry();
            existingProfile.setCountry(afterValue);
            String remarks = String.format("Country changed from %s to %s.", beforeValue, afterValue);
            loggingService.sendUpdateLog(userId, clientId.toString(), "Country", beforeValue, afterValue, remarks);
        }

        if (clientProfileUpdateRequest.getPostalCode() != null && !clientProfileUpdateRequest.getPostalCode().equals(existingProfile.getPostalCode())) {
            String beforeValue = existingProfile.getPostalCode();
            String afterValue = clientProfileUpdateRequest.getPostalCode();
            existingProfile.setPostalCode(afterValue);
            String remarks = String.format("Postal code changed from %s to %s.", beforeValue, afterValue);
            loggingService.sendUpdateLog(userId, clientId.toString(), "Postal Code", beforeValue, afterValue, remarks);
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