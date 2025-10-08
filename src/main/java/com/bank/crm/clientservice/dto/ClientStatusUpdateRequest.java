package com.bank.crm.clientservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientStatusUpdateRequest {
    @NotNull(message = "Activate field is required")
    private Boolean activate;
}
