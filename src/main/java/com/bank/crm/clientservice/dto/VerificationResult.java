package com.bank.crm.clientservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResult {
    private String clientId;
    private ExtractedData extractedData;
    private String timestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedData {
        private List<String> text;
        private Map<String, String> keyValuePairs;
        private List<Object> tables;
    }
}

