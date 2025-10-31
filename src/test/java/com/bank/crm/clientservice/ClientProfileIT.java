package com.bank.crm.clientservice;

import com.bank.crm.clientservice.dto.ClientProfileCreateRequest;
import com.bank.crm.clientservice.dto.ClientStatusUpdateRequest;
import com.bank.crm.clientservice.models.ClientProfile;
import com.bank.crm.clientservice.models.enums.ClientStatusTypes;
import com.bank.crm.clientservice.models.enums.GenderTypes;
import com.bank.crm.clientservice.repositories.ClientProfileRepository;
import com.bank.crm.clientservice.services.LoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.bank.crm.clientservice.TestDataFactory.validClientProfile;
import static com.bank.crm.clientservice.TestDataFactory.validClientProfileUpdateRequest;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.EntityManager;


@Transactional
@Testcontainers
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "aws.sqs.logging-queue-url=http://localhost:4566/000000000000/test-queue",
        "aws.sqs.verification_request_queue_url=http://localhost:4566/000000000000/test-verification-request-queue",
        "aws.sqs.verification_results_queue_url=http://localhost:4566/000000000000/test-verification-results-queue",
        "verification.polling.enabled=false"
})
class ClientProfileIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientProfileRepository clientProfileRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LoggingService loggingService;



    @Test
    void shouldCreateClientSuccessfully() throws Exception {
        ClientProfileCreateRequest newClient = TestDataFactory.validClientProfileCreateRequest();

        mvc.perform(post("/client-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newClient)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").exists())
                .andExpect(jsonPath("$.firstName", is(newClient.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(newClient.getLastName())))
                .andExpect(jsonPath("$.emailAddress", is(newClient.getEmailAddress())))
                .andExpect(jsonPath("$.phoneNumber", is(newClient.getPhoneNumber())));

        assertTrue(clientProfileRepository.existsByEmailAddress(newClient.getEmailAddress()));
        assertTrue(clientProfileRepository.existsByPhoneNumber(newClient.getPhoneNumber()));
    }

    @Test
    void shouldFailWhenEmailNotUniqueOnCreate() throws Exception {
        var existingClient = validClientProfile();
        existingClient.setEmailAddress("duplicate@example.com");
        existingClient.setPhoneNumber("+6591111222");
        clientProfileRepository.saveAndFlush(existingClient);

        ClientProfileCreateRequest newClient = TestDataFactory.validClientProfileCreateRequest();
        newClient.setEmailAddress("duplicate@example.com");
        newClient.setPhoneNumber("+6599998888");


        mvc.perform(post("/client-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newClient)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().json("[\"emailAddress\"]"));
    }

    @Test
    void shouldFailWhenPhoneNotUniqueOnCreate() throws Exception {
        var existingClient = validClientProfile();
        existingClient.setEmailAddress("existing@example.com");
        existingClient.setPhoneNumber("+6511111111");
        clientProfileRepository.saveAndFlush(existingClient);

        ClientProfileCreateRequest newClient = TestDataFactory.validClientProfileCreateRequest();
        newClient.setEmailAddress("unique@example.com");
        newClient.setPhoneNumber("+6511111111");

        mvc.perform(post("/client-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newClient)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().json("[\"phoneNumber\"]"));
    }

    @Test
    void shouldFailWhenBothEmailAndPhoneNotUniqueOnCreate() throws Exception {
        var existingClient = validClientProfile();
        existingClient.setEmailAddress("existing@example.com");
        existingClient.setPhoneNumber("+6512345678");
        clientProfileRepository.saveAndFlush(existingClient);

        ClientProfileCreateRequest newClient = TestDataFactory.validClientProfileCreateRequest();
        newClient.setEmailAddress("existing@example.com");
        newClient.setPhoneNumber("+6512345678");

        mvc.perform(post("/client-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newClient)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().json("[\"emailAddress\", \"phoneNumber\"]"));
    }

    @Test
    void shouldFailWhenInvalidEmailFormatOnCreate() throws Exception {
        ClientProfileCreateRequest newClient = TestDataFactory.validClientProfileCreateRequest();
        newClient.setEmailAddress("invalid-email-format");

        mvc.perform(post("/client-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newClient)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("[\"Email address must be valid\"]"));
    }

    @Test
    void shouldFailWhenInvalidPhoneFormatOnCreate() throws Exception {
        ClientProfileCreateRequest newClient = TestDataFactory.validClientProfileCreateRequest();
        newClient.setPhoneNumber("12345");

        mvc.perform(post("/client-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newClient)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("[\"Phone number must start with + and contain 10-15 digits\"]"));
    }

    @Test
    void shouldFailWhenInvalidGenderEnumOnCreate() throws Exception {
        ClientProfileCreateRequest newClient = TestDataFactory.validClientProfileCreateRequest();
        newClient.setGender("Transgender");


        mvc.perform(post("/client-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newClient)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("[\"Gender must be one of Male, Female, Non Binary or Prefer not to say\"]"));
    }

    @Test
    void shouldUpdateClientSuccessfully() throws Exception {
        var existingClientProfile = validClientProfile();
        clientProfileRepository.saveAndFlush(existingClientProfile);
        mvc.perform(put("/client-profile/" + existingClientProfile.getClientId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validClientProfileUpdateRequest())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId", is(existingClientProfile.getClientId().toString())))
                .andExpect(jsonPath("$.firstName", is("ValidFirst")))
                .andExpect(jsonPath("$.lastName", is("ValidLast")))
                .andExpect(jsonPath("$.dateOfBirth", is("1990-01-01")))
                .andExpect(jsonPath("$.gender", is(GenderTypes.MALE.toString())))
                .andExpect(jsonPath("$.emailAddress", is("unique@example.com")))
                .andExpect(jsonPath("$.phoneNumber", is("+6512345678")))
                .andExpect(jsonPath("$.address", is("123 Main Street")))
                .andExpect(jsonPath("$.city", is("Singapore")))
                .andExpect(jsonPath("$.state", is("Singapore")))
                .andExpect(jsonPath("$.country", is("SG")))
                .andExpect(jsonPath("$.postalCode", is("1234")));

        ClientProfile updatedClient = clientProfileRepository.findById(existingClientProfile.getClientId())
                .orElseThrow(() -> new AssertionError("Updated client not found in DB"));

        assertEquals("ValidFirst", updatedClient.getFirstName());
        assertEquals("ValidLast", updatedClient.getLastName());
        assertEquals("unique@example.com", updatedClient.getEmailAddress());
        assertEquals("+6512345678", updatedClient.getPhoneNumber());
        assertEquals(LocalDate.of(1990, 1, 1), updatedClient.getDateOfBirth());
        assertEquals(GenderTypes.MALE, updatedClient.getGender());
        assertEquals("123 Main Street", updatedClient.getAddress());
        assertEquals("Singapore", updatedClient.getCity());
        assertEquals("Singapore", updatedClient.getState());
        assertEquals("SG", updatedClient.getCountry());
        assertEquals("1234", updatedClient.getPostalCode());
    }

    @Test
    void shouldIgnoreNullFieldsDuringUpdate() throws Exception {
        var existingClientProfile = validClientProfile();
        clientProfileRepository.saveAndFlush(existingClientProfile);

        var updateRequest = validClientProfileUpdateRequest();
        updateRequest.setFirstName(null);
        updateRequest.setLastName(null);

        mvc.perform(put("/client-profile/" + existingClientProfile.getClientId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(existingClientProfile.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(existingClientProfile.getLastName())))
                .andExpect(jsonPath("$.emailAddress", is(updateRequest.getEmailAddress())));
    }

    @Test
    void shouldNotFailUniquenessIfBelongsToSameClientOnUpdate() throws Exception {
        var existingClientProfile = validClientProfile();
        clientProfileRepository.saveAndFlush(existingClientProfile);

        var updateRequest = validClientProfileUpdateRequest();
        updateRequest.setEmailAddress(existingClientProfile.getEmailAddress());
        updateRequest.setPhoneNumber(existingClientProfile.getPhoneNumber());

        mvc.perform(put("/client-profile/" + existingClientProfile.getClientId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailWhenClientNotFoundOnUpdate() throws Exception {
        var nonExistentId = UUID.randomUUID();
        var updateRequest = validClientProfileUpdateRequest();

        mvc.perform(put("/client-profile/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Client not found with ID: " + nonExistentId));
    }

    @Test
    void shouldFailWhenEmailNotUniqueOnUpdate() throws Exception {
        var existingClientProfile = validClientProfile();
        clientProfileRepository.saveAndFlush(existingClientProfile);

        var anotherClient = validClientProfile();
        anotherClient.setEmailAddress("existing@example.com");
        anotherClient.setPhoneNumber("+651234567238");
        clientProfileRepository.saveAndFlush(anotherClient);

        var updateRequest = validClientProfileUpdateRequest();
        updateRequest.setEmailAddress("existing@example.com");

        mvc.perform(put("/client-profile/" + existingClientProfile.getClientId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("[\"emailAddress\"]"));
    }

    @Test
    void shouldFailWhenPhoneNotUniqueOnUpdate() throws Exception {
        var existingClientProfile = validClientProfile();
        clientProfileRepository.saveAndFlush(existingClientProfile);

        var anotherClient = validClientProfile();
        anotherClient.setPhoneNumber("+6598765432");
        anotherClient.setEmailAddress("superunique@example.com");
        clientProfileRepository.saveAndFlush(anotherClient);

        var updateRequest = validClientProfileUpdateRequest();
        updateRequest.setPhoneNumber("+6598765432");

        mvc.perform(put("/client-profile/" + existingClientProfile.getClientId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("[\"phoneNumber\"]"));
    }

    @Test
    void shouldFailWhenBothEmailAndPhoneNotUniqueOnUpdate() throws Exception {
        var existingClientProfile = validClientProfile();
        clientProfileRepository.saveAndFlush(existingClientProfile);

        var anotherClient = validClientProfile();
        anotherClient.setEmailAddress("existing@example.com");
        anotherClient.setPhoneNumber("+6598765432");
        clientProfileRepository.saveAndFlush(anotherClient);

        var updateRequest = validClientProfileUpdateRequest();
        updateRequest.setEmailAddress("existing@example.com");
        updateRequest.setPhoneNumber("+6598765432");

        mvc.perform(put("/client-profile/" + existingClientProfile.getClientId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("[\"emailAddress\", \"phoneNumber\"]"));
    }

    @Test
    void shouldFailWhenInvalidGenderEnumOnUpdate() throws Exception {
        var existingClientProfile = validClientProfile();
        clientProfileRepository.saveAndFlush(existingClientProfile);

        var updateRequest = validClientProfileUpdateRequest();
        updateRequest.setGender("INVALID");

        mvc.perform(put("/client-profile/" + existingClientProfile.getClientId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("[\"Gender must be one of Male, Female, Non Binary or Prefer not to say\"]"));
    }

    @Test
    void shouldFailWhenInvalidEmailFormatOnUpdate() throws Exception {
        var existingClientProfile = validClientProfile();
        clientProfileRepository.saveAndFlush(existingClientProfile);

        var updateRequest = validClientProfileUpdateRequest();
        updateRequest.setEmailAddress("invalid-email");

        mvc.perform(put("/client-profile/" + existingClientProfile.getClientId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("[\"Email address must be valid\"]"));
    }

    @Test
    void shouldFailWhenInvalidPhoneFormatOnUpdate() throws Exception {
        var existingClientProfile = validClientProfile();
        clientProfileRepository.saveAndFlush(existingClientProfile);

        var updateRequest = validClientProfileUpdateRequest();
        updateRequest.setPhoneNumber("12345");

        mvc.perform(put("/client-profile/" + existingClientProfile.getClientId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("[\"Phone number must start with + and contain 10-15 digits\"]"));
    }

    @Test
    void shouldPreserveUnchangedFieldsOnUpdate() throws Exception {
        var existingClientProfile = validClientProfile();
        clientProfileRepository.saveAndFlush(existingClientProfile);

        var updateRequest = validClientProfileUpdateRequest();
        updateRequest.setCity(null);
        updateRequest.setCountry(null);

        mvc.perform(put("/client-profile/" + existingClientProfile.getClientId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        var updatedClient = clientProfileRepository.findById(existingClientProfile.getClientId())
                .orElseThrow();

        assertEquals(existingClientProfile.getCity(), updatedClient.getCity());
        assertEquals(existingClientProfile.getCountry(), updatedClient.getCountry());
    }

    @Test
    void shouldGetClientProfileSuccessfully() throws Exception {
        ClientProfile existingClientProfile = validClientProfile();
        clientProfileRepository.saveAndFlush(existingClientProfile);

        mvc.perform(get("/client-profile/" + existingClientProfile.getClientId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(existingClientProfile)));
    }

    @Test
    void shouldFailWhenClientNotFoundOnFetch() throws Exception {
        ClientProfile existingClientProfile = validClientProfile();
        clientProfileRepository.saveAndFlush(existingClientProfile);

        String unknownClientId = UUID.randomUUID().toString();
        mvc.perform(get("/client-profile/" + unknownClientId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(unknownClientId)));
    }

    @Test
    void shouldFailWhenInvalidUUIDOnFetch() throws Exception {
        mvc.perform(get("/client-profile/ssss"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Client Id should be of type UUID"));
    }

    @Test
    void shouldDeleteClientProfileSuccessfully() throws Exception {
        ClientProfile existingClientProfile = validClientProfile();
        clientProfileRepository.saveAndFlush(existingClientProfile);
        mvc.perform(delete("/client-profile/" + existingClientProfile.getClientId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Client profile deleted successfully"));
        ClientProfile updatedClientProfile = clientProfileRepository.findById(existingClientProfile.getClientId())
                .orElseThrow();
        assertEquals(ClientStatusTypes.INACTIVE, updatedClientProfile.getStatus());
    }

    @Test
    void shouldFailWhenClientAlreadyInactiveOnDelete() throws Exception {
        ClientProfile existingClientProfile = validClientProfile();
        existingClientProfile.setStatus(ClientStatusTypes.INACTIVE);
        clientProfileRepository.saveAndFlush(existingClientProfile);
        mvc.perform(delete("/client-profile/" + existingClientProfile.getClientId()))
                .andExpect(status().isNotFound());
        ClientProfile fromDb = clientProfileRepository.findById(existingClientProfile.getClientId())
                .orElseThrow();
        assertEquals(ClientStatusTypes.INACTIVE, fromDb.getStatus());
    }

    @Test
    void shouldFailWhenClientNotFoundOnDelete() throws Exception {
        String id =  UUID.randomUUID().toString();
        mvc.perform(delete("/client-profile/" + id))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(id)));
    }

    @Test
    void shouldFailWhenClientIdNotUUIDOnDelete() throws Exception {
        mvc.perform(delete("/client-profile/sss"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Client Id should be of type UUID"));
    }

    @Test
    void shouldActivatePendingClientSuccessfully() throws Exception {
        var pendingClient = validClientProfile();
        pendingClient.setStatus(ClientStatusTypes.PENDING);
        clientProfileRepository.saveAndFlush(pendingClient);

        var request = new ClientStatusUpdateRequest(true);

        mvc.perform(post("/client-profile/" + pendingClient.getClientId() + "/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId", is(pendingClient.getClientId().toString())))
                .andExpect(jsonPath("$.status", is(ClientStatusTypes.ACTIVE.toString())));

        var updatedClient = clientProfileRepository.findById(pendingClient.getClientId())
                .orElseThrow();
        assertEquals(ClientStatusTypes.ACTIVE, updatedClient.getStatus());
    }

    @Test
    void shouldDeactivatePendingClientSuccessfully() throws Exception {
        var pendingClient = validClientProfile();
        pendingClient.setStatus(ClientStatusTypes.PENDING);
        clientProfileRepository.saveAndFlush(pendingClient);

        var request = new ClientStatusUpdateRequest(false);

        mvc.perform(post("/client-profile/" + pendingClient.getClientId() + "/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId", is(pendingClient.getClientId().toString())))
                .andExpect(jsonPath("$.status", is(ClientStatusTypes.INACTIVE.toString())));

        var updatedClient = clientProfileRepository.findById(pendingClient.getClientId())
                .orElseThrow();
        assertEquals(ClientStatusTypes.INACTIVE, updatedClient.getStatus());
    }

    @Test
    void shouldFailVerificationWhenClientIsActive() throws Exception {
        var activeClient = validClientProfile();
        activeClient.setStatus(ClientStatusTypes.ACTIVE);
        clientProfileRepository.saveAndFlush(activeClient);

        var request = new ClientStatusUpdateRequest(true);

        mvc.perform(post("/client-profile/" + activeClient.getClientId() + "/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Client status must be PENDING to verify"));
    }

    @Test
    void shouldFailVerificationWhenClientIsInactive() throws Exception {
        var inactiveClient = validClientProfile();
        inactiveClient.setStatus(ClientStatusTypes.INACTIVE);
        clientProfileRepository.saveAndFlush(inactiveClient);
        entityManager.flush();
        entityManager.clear();

        var request = new ClientStatusUpdateRequest(true);

        mvc.perform(post("/client-profile/" + inactiveClient.getClientId() + "/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Client status must be PENDING to verify"));
    }

    @Test
    void shouldReturnNotFoundWhenClientDoesNotExistOnVerify() throws Exception {
        var randomId = UUID.randomUUID();
        var request = new ClientStatusUpdateRequest(true);

        mvc.perform(post("/client-profile/" + randomId + "/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string("Client not found with ID: " + randomId));
    }
}
