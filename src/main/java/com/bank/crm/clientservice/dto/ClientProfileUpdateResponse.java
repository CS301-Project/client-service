package com.bank.crm.clientservice.dto;
import com.bank.crm.clientservice.models.enums.ClientStatusTypes;
import com.bank.crm.clientservice.models.enums.GenderTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientProfileUpdateResponse {
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
