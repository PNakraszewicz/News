package com.interview.news.api.service;

import com.interview.news.domain.model.dto.ArticleDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class NewsExternalServiceIntegrationTest {

    @Autowired
    private NewsExternalServiceIntegration newsExternalServiceIntegration;

    @Test
    void testFetchTopHeadlines() {
        List<ArticleDTO> articles = newsExternalServiceIntegration.fetchTopHeadlines("us");

        assertFalse(articles.isEmpty(), "The list of articles should not be empty after fetching data from News API");

        ArticleDTO firstArticle = articles.get(0);

        assertNotNull(firstArticle.source().name(), "The article's source name should not be null");
        assertNotNull(firstArticle.title(), "The article's title should not be null");
        assertNotNull(firstArticle.url(), "The article's URL should not be null");

        articles.forEach(article -> {
            assertNotNull(article.title(), "Each article should have a title");
            assertNotNull(article.url(), "Each article should have a URL");
        });
    }
}

