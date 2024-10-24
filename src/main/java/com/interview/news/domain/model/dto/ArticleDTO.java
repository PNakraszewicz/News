package com.interview.news.domain.model.dto;

import java.time.Instant;

public record ArticleDTO(
        SourceDTO source,
        String author,
        String title,
        String description,
        String url,
        String urlToImage,
        Instant publishedAt,
        String content
) {}
