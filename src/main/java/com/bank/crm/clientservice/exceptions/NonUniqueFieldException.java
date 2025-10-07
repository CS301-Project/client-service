package com.bank.crm.clientservice.exceptions;

import java.util.Arrays;

public class NonUniqueFieldException extends RuntimeException {
    private final String[] invalidFields;

    public NonUniqueFieldException(String[] invalidFields) {
        super("Fields are not unique: " + Arrays.toString(invalidFields));
        this.invalidFields = invalidFields;
    }

    public String[] getInvalidFields() {
        return invalidFields;
    }
}

