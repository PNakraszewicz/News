package com.interview.news.api.service;

import com.interview.news.domain.model.dto.ArticleDTO;
import com.interview.news.domain.model.entity.Article;
import com.interview.news.persistance.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void fetchAndSaveTopHeadlines(String country) {
        List<ArticleDTO> articleDTOs = newsExternalServiceIntegration.fetchTopHeadlines(country);

        List<Article> articles = articleDTOs.stream()
                .map(this::mapToEntity)
                .toList();

        articleRepository.saveAll(articles);
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
}
