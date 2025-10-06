package com.bank.crm.clientservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

import com.bank.crm.clientservice.models.enums.GenderTypes;
import com.bank.crm.clientservice.models.enums.ClientStatusTypes;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientProfileUpdateRequest {

    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-z ]+$", message = "First name must contain only letters and spaces")
    private String firstName;

    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Last name must contain only letters and spaces")
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private GenderTypes gender;

    @Email(message = "Email should be valid")
    private String emailAddress;

    @Pattern(regexp = "^\\+[0-9]{10,15}$", message = "Phone number must start with + and contain 10-15 digits")
    private String phoneNumber;

    @Size(min = 5, max = 100)
    private String address;

    @Size(min = 2, max = 50)
    private String city;

    @Size(min = 2, max = 50)
    private String state;

    @Size(min = 2, max = 5)
    private String country;

    @Size(min = 4, max = 10)
    private String postalCode;

    private ClientStatusTypes status;
}
