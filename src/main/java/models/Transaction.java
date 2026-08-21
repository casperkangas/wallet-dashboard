package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Transaction(
    String id,
    String accountId,
    String categoryId,
    BigDecimal amount,
    String currency,
    LocalDate transactionDate,
    String description,
    String paymentMethod,
    boolean isTransfer,
    LocalDateTime createdAt
) {
    @JsonCreator
    public static Transaction fromJson(
        @JsonProperty("id") String id,
        @JsonProperty("accountId") String accountId,
        @JsonProperty("category") JsonNode categoryNode,
        @JsonProperty("amount") JsonNode amountNode,
        @JsonProperty("recordDate") String recordDateStr,
        @JsonProperty("note") String description,
        @JsonProperty("paymentMethod") String paymentMethod,
        @JsonProperty("recordType") String recordType,
        @JsonProperty("createdAt") String createdAtStr
    ) {
        BigDecimal amount = BigDecimal.ZERO;
        String currency = null;
        if (amountNode != null) {
            if (amountNode.has("value")) amount = new BigDecimal(amountNode.get("value").asText());
            if (amountNode.has("currencyCode")) currency = amountNode.get("currencyCode").asText();
        }
        
        String categoryId = null;
        if (categoryNode != null && categoryNode.has("id")) {
            categoryId = categoryNode.get("id").asText();
        }
        
        LocalDate transactionDate = null;
        if (recordDateStr != null && !recordDateStr.isEmpty()) {
            transactionDate = LocalDateTime.parse(recordDateStr.substring(0, 19)).toLocalDate();
        }
        
        LocalDateTime createdAt = null;
        if (createdAtStr != null && !createdAtStr.isEmpty()) {
            createdAt = LocalDateTime.parse(createdAtStr.substring(0, 19));
        }
        
        boolean isTransfer = "transfer".equalsIgnoreCase(recordType);
        
        return new Transaction(id, accountId, categoryId, amount, currency, transactionDate, description, paymentMethod, isTransfer, createdAt);
    }
}
