package com.bank.crm.clientservice.dto;

import com.bank.crm.clientservice.validation.ValidEnum;
import com.bank.crm.clientservice.models.enums.GenderTypes;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientProfileCreateRequest {

    @NotBlank(message = "First name must not be blank")
    @Size(min = 2, max = 50, message = "First name size must be between {min} and {max}")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "First name must contain only letters and spaces")
    private String firstName;

    @NotBlank(message = "Last name must not be blank")
    @Size(min = 2, max = 50, message = "Last name size must be between {min} and {max}")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Last name must contain only letters and spaces")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    @ValidEnum(enumClass = GenderTypes.class, message = "Gender must be one of Male, Female, Non Binary or Prefer not to say")
    private String gender;

    @NotBlank(message = "Email address must not be blank")
    @Email(message = "Email address must be valid")
    private String emailAddress;

    @NotBlank(message = "Phone number must not be blank")
    @Pattern(regexp = "^\\+[0-9]{10,15}$", message = "Phone number must start with + and contain 10-15 digits")
    private String phoneNumber;

    @NotBlank(message = "Address must not be blank")
    @Size(min = 5, max = 100, message = "Address size must be between {min} and {max}")
    private String address;

    @NotBlank(message = "City must not be blank")
    @Size(min = 2, max = 50, message = "City size must be between {min} and {max}")
    private String city;

    @NotBlank(message = "State must not be blank")
    @Size(min = 2, max = 50, message = "State size must be between {min} and {max}")
    private String state;

    @NotBlank(message = "Country must not be blank")
    @Size(min = 2, max = 5, message = "Country code size must be between {min} and {max}")
    private String country;

    @NotBlank(message = "Postal code must not be blank")
    @Size(min = 4, max = 10, message = "Postal code size must be between {min} and {max}")
    private String postalCode;
}