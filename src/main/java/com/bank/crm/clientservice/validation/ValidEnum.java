package com.bank.crm.clientservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EnumValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {

    String message() default "must be a valid value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    // Specify the enum class to validate against
    Class<? extends Enum<?>> enumClass();
}
