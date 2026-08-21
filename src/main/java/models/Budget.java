package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Budget(
    String id,
    String categoryId,
    BigDecimal limitAmount,
    String period
) {
    @JsonCreator
    public static Budget fromJson(
        @JsonProperty("id") String id,
        @JsonProperty("categoryIds") JsonNode categoryIdsNode,
        @JsonProperty("limit") BigDecimal limitAmount,
        @JsonProperty("type") String period
    ) {
        String categoryId = null;
        if (categoryIdsNode != null && categoryIdsNode.isArray() && categoryIdsNode.size() > 0) {
            categoryId = categoryIdsNode.get(0).asText();
        }
        return new Budget(id, categoryId, limitAmount, period);
    }
}
