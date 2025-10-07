package com.bank.crm.clientservice;

import com.bank.crm.clientservice.dto.ClientProfileUpdateRequest;
import com.bank.crm.clientservice.models.ClientProfile;
import com.bank.crm.clientservice.models.enums.ClientStatusTypes;
import com.bank.crm.clientservice.models.enums.GenderTypes;

import java.time.LocalDate;

public class TestDataFactory {
    public static ClientProfileUpdateRequest validClientProfileUpdateRequest() {
        return ClientProfileUpdateRequest.builder()
                .firstName("ValidFirst")
                .lastName("ValidLast")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("MALE")
                .emailAddress("unique@example.com")
                .phoneNumber("+6512345678")
                .address("123 Main Street")
                .city("Singapore")
                .state("Singapore")
                .country("SG")
                .postalCode("1234")
                .build();
    }

    public static ClientProfile validClientProfile() {
        return ClientProfile.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .gender(GenderTypes.MALE)
                .emailAddress("john.doe@example.com")
                .phoneNumber("+6298765432")
                .address("123 Example Street")
                .city("Johor Bahru")
                .state("Johor")
                .country("MY")
                .postalCode("12345")
                .status(ClientStatusTypes.ACTIVE)
                .build();
    }
}
