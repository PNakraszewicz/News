package com.interview.news.domain.model.dto;

public record ArticleParamsDTO(String country, String category, String sources) {
    public ArticleParamsDTO {
        if (sources != null && (country != null || category != null)) {
            throw new IllegalArgumentException("Source param cannot be mixed with other params");
        }
    }
}
