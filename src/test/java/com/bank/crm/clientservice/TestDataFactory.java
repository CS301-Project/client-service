package com.bank.crm.clientservice;

import com.bank.crm.clientservice.dto.ClientProfileUpdateRequest;
import java.time.LocalDate;

public class TestDataFactory {
    public static ClientProfileUpdateRequest validClientProfileUpdateRequest() {
        ClientProfileUpdateRequest dto = new ClientProfileUpdateRequest();
        dto.setFirstName("ValidFirst");
        dto.setLastName("ValidLast");
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dto.setGender("MALE");
        dto.setEmailAddress("unique@example.com");
        dto.setPhoneNumber("+6512345678");
        dto.setAddress("123 Main Street");
        dto.setCity("Singapore");
        dto.setState("Singapore");
        dto.setCountry("SG");
        dto.setPostalCode("1234");
        return dto;
    }
}
