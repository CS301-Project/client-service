package com.bank.crm.clientservice.controllers;

import com.bank.crm.clientservice.dto.ClientProfileCreateRequest;
import com.bank.crm.clientservice.dto.ClientProfileUpdateRequest;
import com.bank.crm.clientservice.dto.ClientProfileResponse;
import com.bank.crm.clientservice.exceptions.ClientNotFoundException;
import com.bank.crm.clientservice.exceptions.NonUniqueFieldException;
import com.bank.crm.clientservice.models.ClientProfile;
import com.bank.crm.clientservice.services.ClientProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ClientProfileController {
    private final ClientProfileService clientProfileService;

    @PostMapping("/client-profile")
    public ResponseEntity<ClientProfileResponse> createClient(
            @Valid @RequestBody ClientProfileCreateRequest clientProfileCreateRequest
    ) {
        ClientProfileResponse response = clientProfileService.createClientProfile(clientProfileCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/client-profile/{clientId}")
    public ResponseEntity<ClientProfileResponse> updateClientProfile(
            @PathVariable UUID clientId,
            @Valid @RequestBody ClientProfileUpdateRequest clientProfileUpdateRequest
    ) {
        ClientProfileResponse updatedClient = clientProfileService.updateClientProfile(clientId, clientProfileUpdateRequest);
        return ResponseEntity.ok(updatedClient);
    }

    @GetMapping("/client-profile/{clientId}")
    public ResponseEntity<ClientProfile> getClientProfile(
        @Valid @PathVariable UUID clientId
    ){
        ClientProfile clientProfile = clientProfileService.getClientProfile(clientId);
        return ResponseEntity.ok(clientProfile);
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity
                .badRequest()
                .body("Client Id should be of type UUID");
    }

}
