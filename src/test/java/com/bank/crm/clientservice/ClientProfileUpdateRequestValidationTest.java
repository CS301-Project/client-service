package com.bank.crm.clientservice;

import com.bank.crm.clientservice.dto.ClientProfileUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static com.bank.crm.clientservice.TestDataFactory.validClientProfileUpdateRequest;

class ClientProfileUpdateRequestValidationTest {

    private static Validator validator;
    private static ValidatorFactory factory;
    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        factory.close();
    }

    @Test
    void validDtoShouldHaveNoViolations() {
        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        Set<ConstraintViolation<ClientProfileUpdateRequest>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid DTO should have no validation violations");
    }

    @Test
    void firstNameTooShortShouldFail() {
        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setFirstName("J");
        Set<ConstraintViolation<ClientProfileUpdateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    void firstNameWithInvalidCharsShouldFail() {
        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setFirstName("John123");
        Set<ConstraintViolation<ClientProfileUpdateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    void lastNameTooLongShouldFail() {
        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setLastName("A".repeat(51));
        Set<ConstraintViolation<ClientProfileUpdateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    @Test
    void dateOfBirthInFutureShouldFail() {
        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setDateOfBirth(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<ClientProfileUpdateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")));
    }

    @Test
    void invalidGenderShouldFail() {
        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setGender("INVALID");
        Set<ConstraintViolation<ClientProfileUpdateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("gender")));
    }

    @Test
    void invalidEmailShouldFail() {
        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setEmailAddress("invalid-email");
        Set<ConstraintViolation<ClientProfileUpdateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("emailAddress")));
    }

    @Test
    void invalidPhoneNumberShouldFail() {
        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setPhoneNumber("12345678");
        Set<ConstraintViolation<ClientProfileUpdateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")));
    }

    @Test
    void addressTooShortShouldFail() {
        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setAddress("1234");
        Set<ConstraintViolation<ClientProfileUpdateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("address")));
    }

    @Test
    void cityTooShortShouldFail() {
        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setCity("S");
        Set<ConstraintViolation<ClientProfileUpdateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("city")));
    }

    @Test
    void stateTooShortShouldFail() {
        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setState("S");
        Set<ConstraintViolation<ClientProfileUpdateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("state")));
    }

    @Test
    void countryCodeTooShortShouldFail() {
        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setCountry("S");
        Set<ConstraintViolation<ClientProfileUpdateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("country")));
    }

    @Test
    void postalCodeTooLongShouldFail() {
        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setPostalCode("12345678901");
        Set<ConstraintViolation<ClientProfileUpdateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("postalCode")));
    }
}
