package com.bank.crm.clientservice.controllers;

import com.bank.crm.clientservice.dto.ClientProfileUpdateRequest;
import com.bank.crm.clientservice.dto.ClientProfileUpdateResponse;
import com.bank.crm.clientservice.exceptions.ClientNotFoundException;
import com.bank.crm.clientservice.exceptions.NonUniqueFieldException;
import com.bank.crm.clientservice.services.ClientProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(NonUniqueFieldException.class)
    public ResponseEntity<List<String>> handleInvalidInput(NonUniqueFieldException ex) {
        return ResponseEntity.badRequest().body(Arrays.asList(ex.getInvalidFields()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<String>> handleBeanValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ResponseEntity.badRequest().body(errors);
    }
}
