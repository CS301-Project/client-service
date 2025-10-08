package com.bank.crm.clientservice.dto;

import com.bank.crm.clientservice.models.enums.ClientStatusTypes;
import com.bank.crm.clientservice.validation.ValidEnum;
import com.bank.crm.clientservice.models.enums.GenderTypes;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ClientProfileCreateResponse {
    private UUID clientId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private GenderTypes gender;
    private String emailAddress;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private ClientStatusTypes status;
}
