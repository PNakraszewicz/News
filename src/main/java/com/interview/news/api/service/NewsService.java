package com.interview.news.api.service;

import com.interview.news.domain.model.dto.ArticleDTO;
import com.interview.news.domain.model.dto.ArticleParamsDTO;
import com.interview.news.domain.model.dto.SourceDTO;
import com.interview.news.domain.model.entity.Article;
import com.interview.news.persistance.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NewsService {

    private final NewsExternalServiceIntegration newsExternalServiceIntegration;
    private final ArticleRepository articleRepository;

    @Autowired
    public NewsService(NewsExternalServiceIntegration newsExternalServiceIntegration, ArticleRepository articleRepository) {
        this.newsExternalServiceIntegration = newsExternalServiceIntegration;
        this.articleRepository = articleRepository;
    }

    @Transactional
    @Cacheable(value = "topHeadlinesCache", key = "#params")
    public List<ArticleDTO> fetchAndSaveTopHeadlines(ArticleParamsDTO params) {
        List<ArticleDTO> articleDTOs = newsExternalServiceIntegration.fetchTopHeadlines(params);

        List<Article> articles = articleDTOs.stream()
                .map(this::mapToEntity)
                .toList();

        articleRepository.saveAll(articles);

        return articles.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<Article> getNews(int limit, int offset) {
        return articleRepository.findArticlesWithLimitAndOffset(limit, offset);
    }

    private Article mapToEntity(ArticleDTO articleDTO) {
        Article articleEntity = new Article();
        articleEntity.setSourceName(articleDTO.source() != null ? articleDTO.source().name() : null);
        articleEntity.setAuthor(articleDTO.author());
        articleEntity.setTitle(articleDTO.title());
        articleEntity.setDescription(articleDTO.description());
        articleEntity.setUrl(articleDTO.url());
        articleEntity.setUrlToImage(articleDTO.urlToImage());
        articleEntity.setPublishedAt(articleDTO.publishedAt());
        articleEntity.setContent(articleDTO.content());
        return articleEntity;
    }

    private ArticleDTO mapToDTO(Article article) {
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
}
