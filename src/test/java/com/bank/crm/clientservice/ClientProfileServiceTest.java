package com.bank.crm.clientservice;

import com.bank.crm.clientservice.dto.ClientProfileUpdateRequest;
import com.bank.crm.clientservice.dto.ClientProfileUpdateResponse;
import com.bank.crm.clientservice.exceptions.ClientNotFoundException;
import com.bank.crm.clientservice.exceptions.NonUniqueFieldException;
import com.bank.crm.clientservice.models.ClientProfile;
import com.bank.crm.clientservice.models.enums.ClientStatusTypes;
import com.bank.crm.clientservice.models.enums.GenderTypes;
import com.bank.crm.clientservice.repositories.ClientProfileRepository;
import com.bank.crm.clientservice.services.ClientProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;
import static com.bank.crm.clientservice.TestDataFactory.validClientProfileUpdateRequest;

public class ClientProfileServiceTest {

    private ClientProfileService clientProfileService;
    private ClientProfileRepository mockRepo;

    @BeforeEach
    void setUp() {
        mockRepo = mock(ClientProfileRepository.class);
        clientProfileService = new ClientProfileService(mockRepo);
    }

    @Test
    void shouldCreateClientSuccessfully_WhenEmailAndPhoneUnique() {
        ClientProfile request = ClientProfile.builder()
                .firstName("Fraser")
                .lastName("Chua")
                .dateOfBirth(LocalDate.of(2001, 12, 7))
                .gender(GenderTypes.MALE)
                .emailAddress("fraserchua@gmail.com")
                .phoneNumber("+6596211649")
                .address("123 Street")
                .city("Singapore")
                .state("Singapore")
                .country("SG")
                .postalCode("12345")
                .status(ClientStatusTypes.INACTIVE)
                .build();

        when(mockRepo.existsByEmailAddress(request.getEmailAddress())).thenReturn(false);
        when(mockRepo.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(false);
        when(mockRepo.save(any(ClientProfile.class))).thenAnswer(i -> i.getArguments()[0]);

        ClientProfile created = clientProfileService.createClientProfile(request);

        assertEquals("Fraser", created.getFirstName());
        assertEquals("Chua", created.getLastName());
    }

    @Test
    void shouldThrowNonUniqueFieldException_WhenEmailAlreadyExists() {
        ClientProfile request = ClientProfile.builder()
                .emailAddress("existing@example.com")
                .phoneNumber("+6596211649")
                .build();

        when(mockRepo.existsByEmailAddress(request.getEmailAddress())).thenReturn(true);
        when(mockRepo.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(false);

        NonUniqueFieldException exception = assertThrows(NonUniqueFieldException.class,
                () -> clientProfileService.createClientProfile(request));

        assertArrayEquals(new String[]{"emailAddress"}, exception.getInvalidFields());
    }

    @Test
    void shouldThrowNonUniqueFieldException_WhenPhoneAlreadyExists() {
        ClientProfile request = ClientProfile.builder()
                .emailAddress("fraserchua@gmail.com")
                .phoneNumber("+6596211649")
                .build();

        when(mockRepo.existsByEmailAddress(request.getEmailAddress())).thenReturn(false);
        when(mockRepo.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(true);

        NonUniqueFieldException exception = assertThrows(NonUniqueFieldException.class,
                () -> clientProfileService.createClientProfile(request));

        assertArrayEquals(new String[]{"phoneNumber"}, exception.getInvalidFields());
    }

    @Test
    void shouldThrowNonUniqueFieldException_WhenBothEmailAndPhoneAlreadyExist() {
        ClientProfile request = ClientProfile.builder()
                .emailAddress("existing@example.com")
                .phoneNumber("+6596211649")
                .build();

        when(mockRepo.existsByEmailAddress(request.getEmailAddress())).thenReturn(true);
        when(mockRepo.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(true);

        NonUniqueFieldException exception = assertThrows(NonUniqueFieldException.class,
                () -> clientProfileService.createClientProfile(request));

        assertArrayEquals(new String[]{"emailAddress", "phoneNumber"}, exception.getInvalidFields());
    }

    @Test
    void shouldUpdateClientProfileSuccessfully() {
        UUID clientId = UUID.randomUUID();
        ClientProfile existing = new ClientProfile();
        existing.setClientId(clientId);
        existing.setFirstName("OldName");

        when(mockRepo.findById(clientId)).thenReturn(Optional.of(existing));
        when(mockRepo.existsByEmailAddressAndClientIdNot("new@example.com", clientId)).thenReturn(false);
        when(mockRepo.existsByPhoneNumberAndClientIdNot("+6598765432", clientId)).thenReturn(false);
        when(mockRepo.save(existing)).thenReturn(existing);

        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setFirstName("NewName");
        dto.setEmailAddress("new@example.com");
        dto.setPhoneNumber("+6598765432");

        ClientProfileUpdateResponse response = clientProfileService.updateClientProfile(clientId, dto);

        assertEquals("NewName", response.getFirstName());
        assertEquals("new@example.com", response.getEmailAddress());
        assertEquals("+6598765432", response.getPhoneNumber());
    }

    @Test
    void shouldFailClientNotFound() {
        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        assertThrows(ClientNotFoundException.class,
                () -> clientProfileService.updateClientProfile(UUID.randomUUID(), dto));
    }

    @Test
    void shouldFailEmailNotUniqueConstraint() {
        UUID clientId = UUID.randomUUID();
        ClientProfile existing = new ClientProfile();
        existing.setClientId(clientId);

        when(mockRepo.findById(clientId)).thenReturn(Optional.of(existing));
        when(mockRepo.existsByEmailAddressAndClientIdNot("existing@example.com", clientId)).thenReturn(true);

        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setEmailAddress("existing@example.com");

        NonUniqueFieldException exception = assertThrows(NonUniqueFieldException.class,
                () -> clientProfileService.updateClientProfile(clientId, dto));
        assertArrayEquals(new String[]{"emailAddress"}, exception.getInvalidFields());
    }

    @Test
    void shouldFailPhoneNumberNotUniqueConstraint() {
        UUID clientId = UUID.randomUUID();
        ClientProfile existing = new ClientProfile();
        existing.setClientId(clientId);

        when(mockRepo.findById(clientId)).thenReturn(Optional.of(existing));
        when(mockRepo.existsByPhoneNumberAndClientIdNot("+6512345678", clientId)).thenReturn(true);

        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setPhoneNumber("+6512345678");

        NonUniqueFieldException exception = assertThrows(NonUniqueFieldException.class,
                () -> clientProfileService.updateClientProfile(clientId, dto));
        assertArrayEquals(new String[]{"phoneNumber"}, exception.getInvalidFields());
    }

    @Test
    void shouldFailBothEmailAndPhoneNotUnique() {
        UUID clientId = UUID.randomUUID();
        ClientProfile existing = new ClientProfile();
        existing.setClientId(clientId);

        when(mockRepo.findById(clientId)).thenReturn(Optional.of(existing));
        when(mockRepo.existsByEmailAddressAndClientIdNot("existing@example.com", clientId)).thenReturn(true);
        when(mockRepo.existsByPhoneNumberAndClientIdNot("+6512345678", clientId)).thenReturn(true);

        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setEmailAddress("existing@example.com");
        dto.setPhoneNumber("+6512345678");

        NonUniqueFieldException exception = assertThrows(NonUniqueFieldException.class,
                () -> clientProfileService.updateClientProfile(clientId, dto));
        assertArrayEquals(new String[]{"emailAddress", "phoneNumber"}, exception.getInvalidFields());
    }

    @Test
    void shouldIgnoreNullFieldsDuringUpdate() {
        UUID clientId = UUID.randomUUID();
        ClientProfile existing = new ClientProfile();
        existing.setClientId(clientId);
        existing.setFirstName("OldName");

        when(mockRepo.findById(clientId)).thenReturn(Optional.of(existing));
        when(mockRepo.existsByEmailAddressAndClientIdNot(null, clientId)).thenReturn(false);
        when(mockRepo.existsByPhoneNumberAndClientIdNot(null, clientId)).thenReturn(false);
        when(mockRepo.save(existing)).thenReturn(existing);

        ClientProfileUpdateRequest dto = new ClientProfileUpdateRequest();
        dto.setFirstName(null);

        ClientProfileUpdateResponse response = clientProfileService.updateClientProfile(clientId, dto);
        assertEquals("OldName", response.getFirstName());
    }

    @Test
    void shouldNotFailUniquenessIfBelongsToSameClient() {
        UUID clientId = UUID.randomUUID();
        ClientProfile existing = new ClientProfile();
        existing.setClientId(clientId);
        existing.setEmailAddress("existing@example.com");
        existing.setPhoneNumber("+6512345678");

        when(mockRepo.findById(clientId)).thenReturn(Optional.of(existing));
        when(mockRepo.existsByEmailAddressAndClientIdNot("existing@example.com", clientId)).thenReturn(false);
        when(mockRepo.existsByPhoneNumberAndClientIdNot("+6512345678", clientId)).thenReturn(false);
        when(mockRepo.save(existing)).thenReturn(existing);

        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setEmailAddress("existing@example.com");
        dto.setPhoneNumber("+6512345678");

        ClientProfileUpdateResponse response = clientProfileService.updateClientProfile(clientId, dto);
        assertEquals("existing@example.com", response.getEmailAddress());
        assertEquals("+6512345678", response.getPhoneNumber());
    }
}
