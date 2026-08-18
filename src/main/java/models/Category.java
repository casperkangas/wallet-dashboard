package models;

public record Category(
    String id,
    String name,
    String parentId,
    String icon
) {}
