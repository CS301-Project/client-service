package com.bank.crm.clientservice;

import com.bank.crm.clientservice.dto.ClientProfileUpdateRequest;
import com.bank.crm.clientservice.dto.ClientProfileUpdateResponse;
import com.bank.crm.clientservice.exceptions.ClientNotFoundException;
import com.bank.crm.clientservice.exceptions.NonUniqueFieldException;
import com.bank.crm.clientservice.models.ClientProfile;
import com.bank.crm.clientservice.repositories.ClientProfileRepository;
import com.bank.crm.clientservice.services.ClientProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
