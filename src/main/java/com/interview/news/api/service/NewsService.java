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
import java.util.logging.Logger;

@Service
public class NewsService {
    private static final Logger LOGGER = Logger.getLogger(NewsService.class.getName());
    private final NewsExternalServiceIntegration newsExternalServiceIntegration;
    private final ArticleRepository articleRepository;
    private final SourceRepository sourceRepository;

    public NewsService(NewsExternalServiceIntegration newsExternalServiceIntegration,
                       ArticleRepository articleRepository,
                       SourceRepository sourceRepository) {
        this.newsExternalServiceIntegration = newsExternalServiceIntegration;
        this.articleRepository = articleRepository;
        this.sourceRepository = sourceRepository;
    }

    @Transactional
    @Cacheable(value = "topHeadlinesCache", key = "#params")
    public List<ArticleDTO> fetchAndSaveTopHeadlines(ArticleParamsDTO params) {
        List<ArticleDTO> articleDTOs = newsExternalServiceIntegration.fetchTopHeadlines(params);

        List<String> existingUrls = articleRepository.findAllUrls();
        List<Article> articles = articleDTOs.stream()
                .map(ArticleDTO::toEntity)
                .filter(article -> !existingUrls.contains(article.getUrl()))
                .toList();

        articleRepository.saveAll(articles);

        return articles.stream().map(ArticleDTO::fromEntity).toList();
    }

    @Transactional
    public List<SourceDTO> fetchAndSaveSources() {
        List<SourceDTO> sourceDTOs = newsExternalServiceIntegration.fetchSources();
        List<String> allSourcesId = sourceRepository.findAllSourcesId();

        List<Source> sources = sourceDTOs.stream()
                .map(SourceDTO::toEntity)
                .filter(source -> !allSourcesId.contains(source.getSourceId()))
                /* Added a limit to avoid exhausting the NewsAPI call during scheduled article fetching,
                as persisting all sources could otherwise result in excessive API requests.
                 */
                .limit(3)
                .toList();

        List<Source> savedSources = sourceRepository.saveAll(sources);
        LOGGER.info(() -> "Successfully saved" + savedSources.size() + "sources");
        return savedSources.stream().map(SourceDTO::fromEntity).toList();
    }

    public List<Article> getNews(int limit, int offset) {
        return articleRepository.findArticlesWithLimitAndOffset(limit, offset);
    }
}
