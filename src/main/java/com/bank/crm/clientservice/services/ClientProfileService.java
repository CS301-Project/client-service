package com.bank.crm.clientservice.services;

import com.bank.crm.clientservice.dto.ClientProfileUpdateRequest;
import com.bank.crm.clientservice.dto.ClientProfileUpdateResponse;
import com.bank.crm.clientservice.exceptions.ClientNotFoundException;
import com.bank.crm.clientservice.models.ClientProfile;
import com.bank.crm.clientservice.repositories.ClientProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientProfileService {
    private final ClientProfileRepository clientProfileRepository;

    public ClientProfileUpdateResponse updateClientProfile(UUID clientId, ClientProfileUpdateRequest clientProfileUpdateRequest) {
        ClientProfile existing = clientProfileRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
        mergeClientProfile(existing, clientProfileUpdateRequest);
        ClientProfile updated = clientProfileRepository.save(existing);
        return mapToClientProfileUpdateResponse(updated);
    }

    private void mergeClientProfile(ClientProfile existing, ClientProfileUpdateRequest updateRequest) {
        Optional.ofNullable(updateRequest.getFirstName()).ifPresent(existing::setFirstName);
        Optional.ofNullable(updateRequest.getLastName()).ifPresent(existing::setLastName);
        Optional.ofNullable(updateRequest.getDateOfBirth()).ifPresent(existing::setDateOfBirth);
        Optional.ofNullable(updateRequest.getGender()).ifPresent(existing::setGender);
        Optional.ofNullable(updateRequest.getEmailAddress()).ifPresent(existing::setEmailAddress);
        Optional.ofNullable(updateRequest.getPhoneNumber()).ifPresent(existing::setPhoneNumber);
        Optional.ofNullable(updateRequest.getAddress()).ifPresent(existing::setAddress);
        Optional.ofNullable(updateRequest.getCity()).ifPresent(existing::setCity);
        Optional.ofNullable(updateRequest.getState()).ifPresent(existing::setState);
        Optional.ofNullable(updateRequest.getCountry()).ifPresent(existing::setCountry);
        Optional.ofNullable(updateRequest.getPostalCode()).ifPresent(existing::setPostalCode);
        Optional.ofNullable(updateRequest.getStatus()).ifPresent(existing::setStatus);
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
                .status(clientProfile.getStatus())
                .build();
    }

}