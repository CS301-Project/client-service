package com.bank.crm.clientservice;

import com.bank.crm.clientservice.controllers.ClientProfileController;
import com.bank.crm.clientservice.dto.ClientProfileCreateRequest;
import com.bank.crm.clientservice.dto.ClientProfileCreateResponse;
import com.bank.crm.clientservice.dto.ClientProfileUpdateRequest;
import com.bank.crm.clientservice.dto.ClientProfileUpdateResponse;
import com.bank.crm.clientservice.exceptions.ClientNotFoundException;
import com.bank.crm.clientservice.exceptions.NonUniqueFieldException;
import com.bank.crm.clientservice.models.enums.ClientStatusTypes;
import com.bank.crm.clientservice.models.enums.GenderTypes;
import com.bank.crm.clientservice.services.ClientProfileService;
import com.bank.crm.clientservice.models.ClientProfile;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.print.attribute.standard.Media;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ClientProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientProfileService clientProfileService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnClientData_WhenValidRequest() throws Exception {
        ClientProfileCreateRequest request = TestDataFactory.validClientProfileCreateRequest();

        ClientProfileCreateResponse savedClient = ClientProfileCreateResponse.builder()
                .clientId(UUID.randomUUID())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(GenderTypes.valueOf(request.getGender().toUpperCase()))
                .emailAddress(request.getEmailAddress())
                .address(request.getAddress())
                .postalCode(request.getPostalCode())
                .status(ClientStatusTypes.INACTIVE)
                .build();

        Mockito.when(clientProfileService.createClientProfile(any(ClientProfileCreateRequest.class)))
                .thenReturn(savedClient);

        mockMvc.perform(post("/client-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(savedClient.getClientId().toString()))
                .andExpect(jsonPath("$.firstName").value(request.getFirstName()))
                .andExpect(jsonPath("$.emailAddress").value(request.getEmailAddress()));
    }

    @Test
    void shouldReturnBadRequest_WhenInvalidInput() throws Exception {
        ClientProfileCreateRequest invalidClient = ClientProfileCreateRequest.builder()
                .lastName("chua")
                .emailAddress("not-an-email")
                .build();

        mockMvc.perform(post("/client-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidClient)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_WhenServiceThrowsNonUniqueFieldException() throws Exception {
        ClientProfileCreateRequest request = TestDataFactory.validClientProfileCreateRequest();

        Mockito.when(clientProfileService.createClientProfile(any(ClientProfileCreateRequest.class)))
                .thenThrow(new NonUniqueFieldException(new String[]{"emailAddress", "phoneNumber"}));

        mockMvc.perform(post("/client-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0]").value("emailAddress"))
                .andExpect(jsonPath("$[1]").value("phoneNumber"));
    }


    @Test
    void shouldReturnOkWhenUpdateSuccessful() throws Exception {
        UUID clientId = UUID.randomUUID();

        when(clientProfileService.updateClientProfile(eq(clientId), any(ClientProfileUpdateRequest.class)))
                .thenReturn(new ClientProfileUpdateResponse());

        ClientProfileUpdateRequest requestDto = new ClientProfileUpdateRequest();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(put("/client-profile/" + clientId)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundWhenClientNotFound() throws Exception {
        UUID clientId = UUID.randomUUID();

        when(clientProfileService.updateClientProfile(eq(clientId), any(ClientProfileUpdateRequest.class)))
                .thenThrow(new ClientNotFoundException(clientId));

        ClientProfileUpdateRequest requestDto = new ClientProfileUpdateRequest();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(put("/client-profile/" + clientId)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenDtoFailsValidation() throws Exception {
        UUID clientId = UUID.randomUUID();
        ClientProfileUpdateRequest invalidDto = new ClientProfileUpdateRequest();
        invalidDto.setFirstName("J");

        String requestBody = objectMapper.writeValueAsString(invalidDto);

        mockMvc.perform(put("/client-profile/" + clientId)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenServiceThrowsNonUniqueFieldException() throws Exception {
        UUID clientId = UUID.randomUUID();

        when(clientProfileService.updateClientProfile(eq(clientId), any(ClientProfileUpdateRequest.class)))
                .thenThrow(new NonUniqueFieldException(new String[]{"firstName", "email"}));

        ClientProfileUpdateRequest requestDto = new ClientProfileUpdateRequest();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(put("/client-profile/" + clientId)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
