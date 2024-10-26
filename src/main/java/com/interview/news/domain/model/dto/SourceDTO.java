package com.interview.news.domain.model.dto;

public record SourceDTO(String id, String name, String description, String url,
                        String category, String language, String country) {
    public SourceDTO(String sourceId, String name) {

        this(sourceId, name, null, null, null, null, null);
    }
}
