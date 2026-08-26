package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Budget(
    String id,
    String name,
    String categoryId,
    BigDecimal limitAmount,
    String period,
    boolean closed,
    String startDate,
    String endDate,
    String closedDate
) {
    @JsonCreator
    public static Budget fromJson(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("categoryIds") JsonNode categoryIdsNode,
        @JsonProperty("limit") BigDecimal limitAmount,
        @JsonProperty("type") String period,
        @JsonProperty("closed") Boolean closed,
        @JsonProperty("startDate") String startDate,
        @JsonProperty("endDate") String endDate,
        @JsonProperty("closedDate") String closedDate
    ) {
        String categoryId = null;
        if (categoryIdsNode != null && categoryIdsNode.isArray() && categoryIdsNode.size() > 0) {
            java.util.List<String> ids = new java.util.ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode node : categoryIdsNode) {
                ids.add(node.asText());
            }
            categoryId = String.join(",", ids);
        }
        boolean isClosed = closed != null && closed;
        return new Budget(id, name, categoryId, limitAmount, period, isClosed, startDate, endDate, closedDate);
    }
}
