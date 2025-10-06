package com.bank.crm.clientservice.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

import com.bank.crm.clientservice.models.enums.GenderTypes;
import com.bank.crm.clientservice.models.enums.ClientStatusTypes;
@Entity
@Table(name = "client_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientProfile {

    @Id
    @GeneratedValue
    private UUID clientId;

    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-z ]+$", message = "First name must contain only letters and spaces")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Last name must contain only letters and spaces")
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    @NotNull
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private GenderTypes gender;

    @Column(name = "email_address", nullable = false, unique = true, length = 255)
    @NotBlank
    @Email(message = "Email should be valid")
    private String emailAddress;

    @Column(name = "phone_number", nullable = false, unique = true, length = 16)
    @NotBlank
    @Pattern(regexp = "^\\+[0-9]{10,15}$", message = "Phone number must start with + and contain 10-15 digits")
    private String phoneNumber;

    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(min = 5, max = 100)
    private String address;

    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(min = 2, max = 50)
    private String city;

    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(min = 2, max = 50)
    private String state;

    @Column(nullable = false, length = 5)
    @NotBlank
    @Size(min = 2, max = 5)
    private String country;

    @Column(name = "postal_code", nullable = false, length = 10)
    @NotBlank
    @Size(min = 4, max = 10)
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private ClientStatusTypes status;
}
