package com.bank.crm.clientservice.exceptions;

import java.util.Arrays;

public class InvalidInputException extends RuntimeException {
    private final String[] invalidFields;

    public InvalidInputException(String[] invalidFields) {
        super("Invalid inputs: " + Arrays.toString(invalidFields));
        this.invalidFields = invalidFields;
    }

    public String[] getInvalidFields() {
        return invalidFields;
    }
}

