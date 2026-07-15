package com.ralsei.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ralsei.dto.response.staffrefund.StaffRefundBankDestinationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Parses {@code refund.callbackData} JSON produced during ticket cancellation
 * into a staff-friendly bank destination DTO.
 */
@Slf4j
@Component
@RequiredArgsConstructor
/**
 * Provides the refund callback data parser component for the application.
 */
public class RefundCallbackDataParser {

    private final ObjectMapper objectMapper;

    /**
     * Parses the stored callback JSON into bank destination fields.
     *
     * @param callbackData raw JSON string persisted on the refund row
     * @return parsed bank destination, or {@code null} when input is blank or invalid
     */
    /**
     * Executes the parse operation.
     *
     * @param callbackData the value supplied for this operation
     *
     * @return the operation result
     */
    public StaffRefundBankDestinationResponse parse(String callbackData) {
        if (callbackData == null || callbackData.isBlank()) {
            return null;
        }

        try {
            JsonNode root = objectMapper.readTree(callbackData.trim());
            String bankName = readText(root, "bankName");
            String accountHolder = readText(root, "accountHolder");
            String accountNumber = readText(root, "accountNumber");

            if (bankName == null && accountHolder == null && accountNumber == null) {
                return null;
            }

            return new StaffRefundBankDestinationResponse(bankName, accountHolder, accountNumber);
        } catch (Exception exception) {
            log.warn("Unable to parse refund callbackData: {}", exception.getMessage());
            return null;
        }
    }

    private String readText(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText(null);
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
