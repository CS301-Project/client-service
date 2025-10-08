package com.bank.crm.clientservice;

import com.bank.crm.clientservice.dto.ClientStatusUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ClientStatusUpdateRequestValidationTest {
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
    void trueActivateShouldPass() {
        ClientStatusUpdateRequest dto = new ClientStatusUpdateRequest(true);
        Set<ConstraintViolation<ClientStatusUpdateRequest>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "True value for activate is valid and should have no violations");
    }

    @Test
    void falseActivateShouldPass() {
        ClientStatusUpdateRequest dto = new ClientStatusUpdateRequest(false);
        Set<ConstraintViolation<ClientStatusUpdateRequest>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "False value for activate is valid and should have no violations");
    }

    @Test
    void nullActivateShouldFail() {
        ClientStatusUpdateRequest dto = new ClientStatusUpdateRequest(null);
        Set<ConstraintViolation<ClientStatusUpdateRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Null activate should trigger validation violation");
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("activate") &&
                                v.getMessage().equals("Activate field is required")),
                "Violation should be on the 'activate' field with the correct message");
    }

}
