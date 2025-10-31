package com.bank.crm.clientservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequest {
    private String clientId;
    private String clientEmail;
    private String agent_Id;
    private String agentEmail;
    private String timestamp;
}

