package com.bank.crm.clientservice;

import com.bank.crm.clientservice.dto.ClientProfileCreateRequest;
import com.bank.crm.clientservice.dto.ClientProfileCreateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Set;

import static com.bank.crm.clientservice.TestDataFactory.validClientProfileUpdateRequest;
import static org.junit.jupiter.api.Assertions.*;
import static com.bank.crm.clientservice.TestDataFactory.validClientProfileCreateRequest;


public class ClientProfileCreateRequestValidationTest {
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
        ClientProfileCreateRequest dto = validClientProfileCreateRequest();
        Set<ConstraintViolation<ClientProfileCreateRequest>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid DTO should have no validation violations");
    }

    @Test
    void firstNameTooShortShouldFail() {
        ClientProfileCreateRequest dto = validClientProfileCreateRequest();
        dto.setFirstName("J");
        Set<ConstraintViolation<ClientProfileCreateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    void firstNameWithInvalidCharsShouldFail() {
        ClientProfileCreateRequest dto = validClientProfileCreateRequest();
        dto.setFirstName("John123");
        Set<ConstraintViolation<ClientProfileCreateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    void lastNameTooLongShouldFail() {
        ClientProfileCreateRequest dto = validClientProfileCreateRequest();
        dto.setLastName("A".repeat(51));
        Set<ConstraintViolation<ClientProfileCreateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    @Test
    void dateOfBirthInFutureShouldFail() {
        ClientProfileCreateRequest dto = validClientProfileCreateRequest();
        dto.setDateOfBirth(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<ClientProfileCreateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")));
    }

    @Test
    void invalidGenderShouldFail() {
        ClientProfileCreateRequest dto = validClientProfileCreateRequest();
        dto.setGender("INVALID");
        Set<ConstraintViolation<ClientProfileCreateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("gender")));
    }

    @Test
    void invalidEmailShouldFail() {
        ClientProfileCreateRequest dto = validClientProfileCreateRequest();
        dto.setEmailAddress("invalid-email");
        Set<ConstraintViolation<ClientProfileCreateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("emailAddress")));
    }

    @Test
    void invalidPhoneNumberShouldFail() {
        ClientProfileCreateRequest dto = validClientProfileCreateRequest();
        dto.setPhoneNumber("12345678");
        Set<ConstraintViolation<ClientProfileCreateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")));
    }

    @Test
    void addressTooShortShouldFail() {
        ClientProfileCreateRequest dto = validClientProfileCreateRequest();
        dto.setAddress("1234");
        Set<ConstraintViolation<ClientProfileCreateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("address")));
    }

    @Test
    void cityTooShortShouldFail() {
        ClientProfileCreateRequest dto = validClientProfileCreateRequest();
        dto.setCity("S");
        Set<ConstraintViolation<ClientProfileCreateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("city")));
    }

    @Test
    void stateTooShortShouldFail() {
        ClientProfileCreateRequest dto = validClientProfileCreateRequest();
        dto.setState("S");
        Set<ConstraintViolation<ClientProfileCreateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("state")));
    }

    @Test
    void countryCodeTooShortShouldFail() {
        ClientProfileCreateRequest dto = validClientProfileCreateRequest();
        dto.setCountry("S");
        Set<ConstraintViolation<ClientProfileCreateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("country")));
    }

    @Test
    void postalCodeTooLongShouldFail() {
        ClientProfileCreateRequest dto = validClientProfileCreateRequest();
        dto.setPostalCode("12345678901");
        Set<ConstraintViolation<ClientProfileCreateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("postalCode")));
    }
}
