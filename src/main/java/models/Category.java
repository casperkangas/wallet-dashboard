package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Category(
    String id,
    String name,
    String parentId,
    String icon
) {
    @JsonCreator
    public static Category fromJson(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("group") JsonNode groupNode,
        @JsonProperty("color") String color
    ) {
        String parentId = null;
        if (groupNode != null && groupNode.has("id")) {
            parentId = groupNode.get("id").asText();
        }
        return new Category(id, name, parentId, color);
    }
}
