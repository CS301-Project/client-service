package com.bank.crm.clientservice;

import com.bank.crm.clientservice.controllers.ClientProfileController;
import com.bank.crm.clientservice.dto.ClientProfileUpdateRequest;
import com.bank.crm.clientservice.dto.ClientProfileUpdateResponse;
import com.bank.crm.clientservice.exceptions.ClientNotFoundException;
import com.bank.crm.clientservice.exceptions.InvalidInputException;
import com.bank.crm.clientservice.services.ClientProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ClientProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientProfileService clientProfileService;

    @Autowired
    private ObjectMapper objectMapper;

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
    void shouldReturnBadRequestWhenInvalidInput() throws Exception {
        UUID clientId = UUID.randomUUID();

        when(clientProfileService.updateClientProfile(eq(clientId), any(ClientProfileUpdateRequest.class)))
                .thenThrow(new InvalidInputException(new String[]{"firstName", "email"}));

        ClientProfileUpdateRequest requestDto = new ClientProfileUpdateRequest();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(put("/client-profile/" + clientId)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

}
