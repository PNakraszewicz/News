package com.interview.news.domain.model.dto;

public record ArticleParamsDTO(String country, String category, String sources) {
    public ArticleParamsDTO {
        if ((country != null && (category != null || sources != null)) ||
                (category != null && sources != null)) {
            throw new IllegalArgumentException("Source param cannot be mixed with other params");
        }
    }
}
