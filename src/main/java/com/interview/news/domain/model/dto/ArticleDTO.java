package com.interview.news.domain.model.dto;

import com.interview.news.domain.model.entity.Article;

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
) {
    public static ArticleDTO fromEntity(Article article) {
        return new ArticleDTO(
                new SourceDTO(null, article.getSourceName()),
                article.getAuthor(),
                article.getTitle(),
                article.getDescription(),
                article.getUrl(),
                article.getUrlToImage(),
                article.getPublishedAt(),
                article.getContent()
        );
    }

    public Article toEntity() {
        Article article = new Article();
        article.setSourceName(this.source() != null ? this.source().name() : null);
        article.setAuthor(this.author());
        article.setTitle(this.title());
        article.setDescription(this.description());
        article.setUrl(this.url());
        article.setUrlToImage(this.urlToImage());
        article.setPublishedAt(this.publishedAt());
        article.setContent(this.content());
        return article;
    }
}
