package com.bank.crm.clientservice.controllers;

import com.bank.crm.clientservice.dto.ClientProfileUpdateRequest;
import com.bank.crm.clientservice.dto.ClientProfileUpdateResponse;
import com.bank.crm.clientservice.exceptions.ClientNotFoundException;
import com.bank.crm.clientservice.exceptions.InvalidInputException;
import com.bank.crm.clientservice.services.ClientProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ClientProfileController {
    private final ClientProfileService clientProfileService;

    @PutMapping("/client-profile/{clientId}")
    public ResponseEntity<ClientProfileUpdateResponse> updateClientProfile(
            @PathVariable UUID clientId,
            @Valid @RequestBody ClientProfileUpdateRequest clientProfileUpdateRequest
    ) {
        ClientProfileUpdateResponse updatedClient = clientProfileService.updateClientProfile(clientId, clientProfileUpdateRequest);
        return ResponseEntity.ok(updatedClient);
    }

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<String> handleClientNotFound(ClientNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<List<String>> handleInvalidInput(InvalidInputException ex) {
        return ResponseEntity.badRequest().body(Arrays.asList(ex.getInvalidFields()));
    }
}
