package com.interview.news.domain.model.dto;

import com.interview.news.domain.model.entity.Source;

public record SourceDTO(String id, String name, String description, String url,
                        String category, String language, String country) {
    public SourceDTO(String sourceId, String name) {

        this(sourceId, name, null, null, null, null, null);
    }

    public static SourceDTO fromEntity(Source source) {
        return new SourceDTO(
                source.getSourceId(),
                source.getName(),
                source.getDescription(),
                source.getUrl(),
                source.getCategory(),
                source.getLanguage(),
                source.getCountry()
        );
    }

    public Source toEntity() {
        Source source = new Source();
        source.setSourceId(this.id());
        source.setName(this.name());
        source.setDescription(this.description());
        source.setUrl(this.url());
        source.setCategory(this.category());
        source.setLanguage(this.language());
        source.setCountry(this.country());
        return source;
    }
}
