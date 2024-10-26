package com.interview.news.api.service;

import com.interview.news.domain.model.dto.ArticleDTO;
import com.interview.news.domain.model.dto.ArticleParamsDTO;
import com.interview.news.domain.model.dto.SourceDTO;
import com.interview.news.domain.model.entity.Article;
import com.interview.news.domain.model.entity.Source;
import com.interview.news.persistance.ArticleRepository;
import com.interview.news.persistance.SourceRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NewsService {

    private final NewsExternalServiceIntegration newsExternalServiceIntegration;
    private final ArticleRepository articleRepository;
    private final SourceRepository sourceRepository;

    public NewsService(NewsExternalServiceIntegration newsExternalServiceIntegration, ArticleRepository articleRepository, SourceRepository sourceRepository) {
        this.newsExternalServiceIntegration = newsExternalServiceIntegration;
        this.articleRepository = articleRepository;
        this.sourceRepository = sourceRepository;
    }

    @Transactional
    @Cacheable(value = "topHeadlinesCache", key = "#params")
    public List<ArticleDTO> fetchAndSaveTopHeadlines(ArticleParamsDTO params) {
        List<ArticleDTO> articleDTOs = newsExternalServiceIntegration.fetchTopHeadlines(params);

        List<Article> articles = articleDTOs.stream()
                .map(this::mapToEntity)
                .filter(article -> !articleRepository.existsByUrl(article.getUrl())) // Filtruje duplikaty
                .toList();

        articleRepository.saveAll(articles);

        return articles.stream().map(this::mapToDTO).toList();
    }

    @Transactional
    public List<SourceDTO> getSources() {
        List<SourceDTO> sourceDTOs = newsExternalServiceIntegration.fetchSources();

        List<Source> sources = sourceDTOs.stream()
                .map(this::mapToEntity)
                .filter(source -> !sourceRepository.existsBySourceId(source.getSourceId())) // Filtruje duplikaty
                .toList();

        List<Source> savedSources = sourceRepository.saveAll(sources);
        return savedSources.stream().map(this::mapToDTO).toList();
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

    private Source mapToEntity(SourceDTO sourceDTO) {
        Source sourceEntity = new Source();
        sourceEntity.setSourceId(sourceDTO.id());
        sourceEntity.setName(sourceDTO.name());
        sourceEntity.setDescription(sourceDTO.description());
        sourceEntity.setUrl(sourceDTO.url());
        sourceEntity.setCategory(sourceDTO.category());
        sourceEntity.setLanguage(sourceDTO.language());
        sourceEntity.setCountry(sourceDTO.country());
        return sourceEntity;
    }

    private SourceDTO mapToDTO(Source source) {
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
}
