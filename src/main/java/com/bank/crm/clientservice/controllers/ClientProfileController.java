package com.bank.crm.clientservice.controllers;

import com.bank.crm.clientservice.dto.*;
import com.bank.crm.clientservice.exceptions.ClientNotFoundException;
import com.bank.crm.clientservice.exceptions.ClientNotPendingException;
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
@RequestMapping("/client-profile")
@RequiredArgsConstructor
public class ClientProfileController {
    private final ClientProfileService clientProfileService;

    @PostMapping
    public ResponseEntity<ClientProfileResponse> createClient(
            @Valid @RequestBody ClientProfileCreateRequest clientProfileCreateRequest,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "SYSTEM") String userId
    ) {
        ClientProfileResponse response = clientProfileService.createClientProfile(clientProfileCreateRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<ClientProfileResponse> updateClientProfile(
            @PathVariable UUID clientId,
            @Valid @RequestBody ClientProfileUpdateRequest clientProfileUpdateRequest,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "SYSTEM") String userId
    ) {
        ClientProfileResponse updatedClient = clientProfileService.updateClientProfile(clientId, clientProfileUpdateRequest, userId);
        return ResponseEntity.ok(updatedClient);
    }

    @PostMapping("/{clientId}/verify")
    public ResponseEntity<ClientStatusResponse> verifyClient(
            @PathVariable UUID clientId,
            @Valid @RequestBody ClientStatusUpdateRequest request
    ) {
        ClientStatusResponse response = clientProfileService.updateClientStatus(clientId, request.getActivate());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<String> deleteClientProfile(
            @Valid @PathVariable UUID clientId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "SYSTEM") String userId
    ) {
        clientProfileService.deleteClientProfile(clientId, userId);
        return ResponseEntity.ok("Client profile deleted successfully");
    }


    @GetMapping("/{clientId}")
    public ResponseEntity<ClientProfile> getClientProfile(@Valid @PathVariable UUID clientId) {
        ClientProfile clientProfile = clientProfileService.getClientProfile(clientId);
        return ResponseEntity.ok(clientProfile);
    }

    @GetMapping
    public ResponseEntity<List<ClientProfile>> getAllClientProfiles() {
        List<ClientProfile> clientProfiles = clientProfileService.getAllClientProfiles();
        return ResponseEntity.ok(clientProfiles);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ClientProfile>> getClientProfiles(@RequestBody List<UUID> clientIds, String userId) {
        List<ClientProfile> clientProfiles = clientProfileService.getClientProfiles(clientIds, userId);
        return ResponseEntity.ok(clientProfiles);
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

    @ExceptionHandler(ClientNotPendingException.class)
    public ResponseEntity<String> handleClientNotPending(ClientNotPendingException ex) {
        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }

}
