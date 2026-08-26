package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Account(
    String id,
    String name,
    String currency,
    BigDecimal balance,
    String institution,
    String accountType,
    LocalDateTime updatedAt
) {
    @JsonCreator
    public static Account fromJson(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("currencyCode") String currency,
        @JsonProperty("balance") JsonNode balanceNode,
        @JsonProperty("institution") String institution,
        @JsonProperty("accountType") String accountType,
        @JsonProperty("updatedAt") LocalDateTime updatedAt
    ) {
        BigDecimal parsedBalance = BigDecimal.ZERO;
        if (balanceNode != null) {
            if (balanceNode.has("rawCurrentBalance")) {
                parsedBalance = new BigDecimal(balanceNode.get("rawCurrentBalance").asText());
            } else if (balanceNode.has("currentBalance")) {
                parsedBalance = new BigDecimal(balanceNode.get("currentBalance").asText());
            } else if (balanceNode.isNumber()) {
                parsedBalance = new BigDecimal(balanceNode.asText());
            } else if (balanceNode.isTextual()) {
                parsedBalance = new BigDecimal(balanceNode.asText());
            }
        }
        return new Account(id, name, currency, parsedBalance, institution, accountType, updatedAt);
    }
}
