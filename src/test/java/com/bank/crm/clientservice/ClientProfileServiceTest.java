package com.bank.crm.clientservice;

import com.bank.crm.clientservice.dto.*;
import com.bank.crm.clientservice.exceptions.ClientNotFoundException;
import com.bank.crm.clientservice.exceptions.ClientNotPendingException;
import com.bank.crm.clientservice.exceptions.NonUniqueFieldException;
import com.bank.crm.clientservice.models.ClientProfile;
import com.bank.crm.clientservice.models.enums.ClientStatusTypes;
import com.bank.crm.clientservice.repositories.ClientProfileRepository;
import com.bank.crm.clientservice.services.ClientProfileService;
import com.bank.crm.clientservice.services.LoggingService;
import com.bank.crm.clientservice.services.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static com.bank.crm.clientservice.TestDataFactory.validClientProfile;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;
import static com.bank.crm.clientservice.TestDataFactory.validClientProfileUpdateRequest;

public class ClientProfileServiceTest {

    private ClientProfileService clientProfileService;
    private ClientProfileRepository mockRepo;
    private LoggingService mockLoggingService;
    private VerificationService mockVerificationService;

    @BeforeEach
    void setUp() {
        mockRepo = mock(ClientProfileRepository.class);
        mockLoggingService = mock(LoggingService.class);
        mockVerificationService = mock(VerificationService.class);
        clientProfileService = new ClientProfileService(mockRepo, mockLoggingService, mockVerificationService);
    }

    @Test
    void shouldCreateClientSuccessfully_WhenEmailAndPhoneUnique() {
        ClientProfileCreateRequest request = TestDataFactory.validClientProfileCreateRequest();

        when(mockRepo.existsByEmailAddress(request.getEmailAddress())).thenReturn(false);
        when(mockRepo.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(false);
        when(mockRepo.save(any(ClientProfile.class))).thenAnswer(invocation -> {
            ClientProfile client = invocation.getArgument(0);
            client.setClientId(UUID.randomUUID());
            client.setStatus(ClientStatusTypes.PENDING);
            return client;
        });
        doNothing().when(mockLoggingService)
                .sendCreateLog(anyString(), anyString(), anyString());
        ClientProfileResponse created = clientProfileService.createClientProfile(request, anyString());

        assertEquals("ValidFirst", created.getFirstName());
        assertEquals("ValidLast", created.getLastName());
        assertNotNull(created.getClientId());
    }

    @Test
    void shouldThrowNonUniqueFieldException_WhenEmailAlreadyExists() {
        ClientProfileCreateRequest request = TestDataFactory.validClientProfileCreateRequest();

        when(mockRepo.existsByEmailAddress(request.getEmailAddress())).thenReturn(true);
        when(mockRepo.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(false);

        NonUniqueFieldException exception = assertThrows(NonUniqueFieldException.class,
                () -> clientProfileService.createClientProfile(request, anyString()));

        assertArrayEquals(new String[]{"emailAddress"}, exception.getInvalidFields());
    }

    @Test
    void shouldThrowNonUniqueFieldException_WhenPhoneAlreadyExists() {
        ClientProfileCreateRequest request = TestDataFactory.validClientProfileCreateRequest();

        when(mockRepo.existsByEmailAddress(request.getEmailAddress())).thenReturn(false);
        when(mockRepo.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(true);

        NonUniqueFieldException exception = assertThrows(NonUniqueFieldException.class,
                () -> clientProfileService.createClientProfile(request, anyString()));

        assertArrayEquals(new String[]{"phoneNumber"}, exception.getInvalidFields());
    }

    @Test
    void shouldThrowNonUniqueFieldException_WhenBothEmailAndPhoneAlreadyExist() {
        ClientProfileCreateRequest request = TestDataFactory.validClientProfileCreateRequest();

        when(mockRepo.existsByEmailAddress(request.getEmailAddress())).thenReturn(true);
        when(mockRepo.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(true);

        NonUniqueFieldException exception = assertThrows(NonUniqueFieldException.class,
                () -> clientProfileService.createClientProfile(request, anyString()));

        assertArrayEquals(new String[]{"emailAddress", "phoneNumber"}, exception.getInvalidFields());
    }

    @Test
    void shouldUpdateClientProfileSuccessfully() {
        UUID clientId = UUID.randomUUID();
        ClientProfile existing = TestDataFactory.validClientProfile();
        when(mockRepo.findById(clientId)).thenReturn(Optional.of(existing));
        when(mockRepo.existsByEmailAddressAndClientIdNot("new@example.com", clientId)).thenReturn(false);
        when(mockRepo.existsByPhoneNumberAndClientIdNot("+6598765432", clientId)).thenReturn(false);
        when(mockRepo.save(existing)).thenReturn(existing);

        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        dto.setFirstName("NewName");
        dto.setEmailAddress("new@example.com");
        dto.setPhoneNumber("+6598765432");
        doNothing().when(mockLoggingService)
                .sendUpdateLog(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        ClientProfileResponse response = clientProfileService.updateClientProfile(clientId, dto, "test-user");

        assertEquals("NewName", response.getFirstName());
        assertEquals("new@example.com", response.getEmailAddress());
        assertEquals("+6598765432", response.getPhoneNumber());
    }

    @Test
    void shouldFailClientNotFoundOnUpdate() {
        ClientProfileUpdateRequest dto = validClientProfileUpdateRequest();
        assertThrows(ClientNotFoundException.class,
                () -> clientProfileService.updateClientProfile(UUID.randomUUID(), dto, "test-user"));
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
                () -> clientProfileService.updateClientProfile(clientId, dto, "test-user"));
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
                () -> clientProfileService.updateClientProfile(clientId, dto, "test-user"));
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
                () -> clientProfileService.updateClientProfile(clientId, dto, "test-user"));
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

        ClientProfileResponse response = clientProfileService.updateClientProfile(clientId, dto, "test-user");
        assertEquals("OldName", response.getFirstName());
    }

    @Test
    void shouldNotFailUniquenessIfBelongsToSameClient() {
        UUID clientId = UUID.randomUUID();
        ClientProfile existing = TestDataFactory.validClientProfile();
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
        doNothing().when(mockLoggingService)
                .sendUpdateLog(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        ClientProfileResponse response = clientProfileService.updateClientProfile(clientId, dto, "test-user");
        assertEquals("existing@example.com", response.getEmailAddress());
        assertEquals("+6512345678", response.getPhoneNumber());
    }

    @Test
    void shouldGetClientProfileSuccessfully() {
        UUID id = UUID.randomUUID();
        ClientProfile existing = new ClientProfile();
        existing.setClientId(id);
        when(mockRepo.findById(id)).thenReturn(Optional.of(existing));
        ClientProfile result = clientProfileService.getClientProfile(id);
        assertEquals(id, result.getClientId());
    }

    @Test
    void shouldFailClientNotFoundOnFetch() {
        UUID id = UUID.randomUUID();
        when(mockRepo.findById(id)).thenReturn(Optional.empty());
        assertThrows(ClientNotFoundException.class,
                () -> clientProfileService.getClientProfile(id));
    }

    @Test
    void shouldUpdateClientStatusToActiveIfPending() {
        UUID clientId = UUID.randomUUID();
        ClientProfile client = validClientProfile();
        client.setClientId(clientId);
        client.setStatus(ClientStatusTypes.PENDING);

        when(mockRepo.findById(clientId)).thenReturn(Optional.of(client));
        when(mockRepo.save(client)).thenReturn(client);

        ClientStatusResponse response = clientProfileService.updateClientStatus(clientId, true, "test-user");
        assertEquals("ACTIVE", response.getStatus());
        assertEquals(clientId, response.getClientId());
    }

    @Test
    void shouldUpdateClientStatusToInactiveIfPending() {
        UUID clientId = UUID.randomUUID();
        ClientProfile client = validClientProfile();
        client.setClientId(clientId);
        client.setStatus(ClientStatusTypes.PENDING);

        when(mockRepo.findById(clientId)).thenReturn(Optional.of(client));
        when(mockRepo.save(client)).thenReturn(client);

        ClientStatusResponse response = clientProfileService.updateClientStatus(clientId, false, "test-user");
        assertEquals("INACTIVE", response.getStatus());
        assertEquals(clientId, response.getClientId());
    }

    @Test
    void shouldThrowExceptionIfClientCurrentlyActive() {
        UUID clientId = UUID.randomUUID();
        ClientProfile client = new ClientProfile();
        client.setClientId(clientId);
        client.setStatus(ClientStatusTypes.ACTIVE);

        when(mockRepo.findById(clientId)).thenReturn(Optional.of(client));

        ClientNotPendingException ex = assertThrows(ClientNotPendingException.class,
                () -> clientProfileService.updateClientStatus(clientId, true, "test-user"));

        assertEquals("Client status must be PENDING to verify", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionIfClientCurrentlyInActive() {
        UUID clientId = UUID.randomUUID();
        ClientProfile client = new ClientProfile();
        client.setClientId(clientId);
        client.setStatus(ClientStatusTypes.INACTIVE);

        when(mockRepo.findById(clientId)).thenReturn(Optional.of(client));

        ClientNotPendingException ex = assertThrows(ClientNotPendingException.class,
                () -> clientProfileService.updateClientStatus(clientId, true, "test-user"));

        assertEquals("Client status must be PENDING to verify", ex.getMessage());
    }

    @Test
    void shouldThrowClientNotFoundWhenUpdatingStatus() {
        UUID clientId = UUID.randomUUID();
        when(mockRepo.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class,
                () -> clientProfileService.updateClientStatus(clientId, true, "test-user"));
    }

    void shouldFailClientStatusInactiveOnFetch() {
        UUID clientId = UUID.randomUUID();
        ClientProfile existing = validClientProfile();
        existing.setClientId(clientId);
        existing.setStatus(ClientStatusTypes.INACTIVE);
        when(mockRepo.findById(clientId)).thenReturn(Optional.of(existing));
        assertThrows(ClientNotFoundException.class,
                () -> clientProfileService.getClientProfile(clientId));
    }

    @Test
    void shouldDeleteClientProfileSuccessfully() {
        UUID clientId = UUID.randomUUID();
        ClientProfile existing = validClientProfile();
        existing.setClientId(clientId);
        when(mockRepo.findById(clientId)).thenReturn(Optional.of(existing));
        when(mockRepo.save(existing)).thenReturn(existing);
        clientProfileService.deleteClientProfile(clientId, anyString());
        assertEquals(ClientStatusTypes.INACTIVE, existing.getStatus());
    }
}
