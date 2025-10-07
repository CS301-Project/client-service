package com.bank.crm.clientservice.services;

import com.bank.crm.clientservice.dto.ClientProfileUpdateRequest;
import com.bank.crm.clientservice.dto.ClientProfileUpdateResponse;
import com.bank.crm.clientservice.exceptions.ClientNotFoundException;
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

    public ClientProfile createClientProfile( ClientProfile clientProfile) {
        validateEmailAndPhoneUniqueness(clientProfile);
        clientProfile.setClientId(UUID.randomUUID());
        clientProfile.setStatus(ClientStatusTypes.INACTIVE);
        return clientProfileRepository.save(clientProfile);
    }


    public ClientProfileUpdateResponse updateClientProfile(UUID clientId, ClientProfileUpdateRequest clientProfileUpdateRequest) {
        ClientProfile existing = clientProfileRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
        validateEmailAndPhoneUniqueness(clientId, clientProfileUpdateRequest);
        mergeClientProfile(existing, clientProfileUpdateRequest);
        ClientProfile updated = clientProfileRepository.save(existing);
        return mapToClientProfileUpdateResponse(updated);
    }

    private void mergeClientProfile(ClientProfile existing, ClientProfileUpdateRequest updateRequest) {
        Optional.ofNullable(updateRequest.getFirstName()).ifPresent(existing::setFirstName);
        Optional.ofNullable(updateRequest.getLastName()).ifPresent(existing::setLastName);
        Optional.ofNullable(updateRequest.getDateOfBirth()).ifPresent(existing::setDateOfBirth);
        Optional.ofNullable(updateRequest.getGender()).map(GenderTypes::fromString).ifPresent(existing::setGender);
        Optional.ofNullable(updateRequest.getEmailAddress()).ifPresent(existing::setEmailAddress);
        Optional.ofNullable(updateRequest.getPhoneNumber()).ifPresent(existing::setPhoneNumber);
        Optional.ofNullable(updateRequest.getAddress()).ifPresent(existing::setAddress);
        Optional.ofNullable(updateRequest.getCity()).ifPresent(existing::setCity);
        Optional.ofNullable(updateRequest.getState()).ifPresent(existing::setState);
        Optional.ofNullable(updateRequest.getCountry()).ifPresent(existing::setCountry);
        Optional.ofNullable(updateRequest.getPostalCode()).ifPresent(existing::setPostalCode);
    }

    private ClientProfileUpdateResponse mapToClientProfileUpdateResponse(ClientProfile clientProfile) {
        return ClientProfileUpdateResponse.builder()
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

    private void validateEmailAndPhoneUniqueness(ClientProfile clientProfile) {
        List<String> errors = Stream.of(
                clientProfile.getEmailAddress() != null && clientProfileRepository.existsByEmailAddress(clientProfile.getEmailAddress())
                        ? "emailAddress" : null,
                clientProfile.getPhoneNumber() != null && clientProfileRepository.existsByPhoneNumber(clientProfile.getPhoneNumber())
                        ? "phoneNumber" : null
        ).filter(Objects::nonNull).toList();

        if (!errors.isEmpty()) {
            throw new NonUniqueFieldException(errors.toArray(new String[0]));
        }
    }

}