package com.bank.crm.clientservice.models.enums;

 public enum GenderTypes {
    MALE,
    FEMALE,
    NON_BINARY,
    PREFER_NOT_TO_SAY;

    public static GenderTypes fromString(String value) {
         return switch (value.toLowerCase()) {
             case "male" -> MALE;
             case "female" -> FEMALE;
             case "prefer not to say" -> PREFER_NOT_TO_SAY;
             default -> throw new IllegalArgumentException("Invalid gender: " + value);
         };
    }
 }
