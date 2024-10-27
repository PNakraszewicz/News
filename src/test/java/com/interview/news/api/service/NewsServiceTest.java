package com.interview.news.api.service;

import com.interview.news.domain.model.dto.ArticleDTO;
import com.interview.news.domain.model.dto.ArticleParamsDTO;
import com.interview.news.domain.model.dto.SourceDTO;
import com.interview.news.domain.model.entity.Article;
import com.interview.news.persistance.ArticleRepository;
import com.interview.news.persistance.SourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NewsServiceTest {

    @Mock
    private NewsExternalServiceIntegration newsExternalServiceIntegration;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private SourceRepository sourceRepository;

    @InjectMocks
    private NewsService newsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldFetchAndSaveUniqueTopHeadlines() {
        ArticleParamsDTO params = new ArticleParamsDTO("us", "technology", null);
        List<ArticleDTO> articleDTOs = List.of(
                new ArticleDTO(new SourceDTO("middle-earth", "Middle-Earth Chronicle"), "Gandalf", "Penguins Invade Middle-Earth",
                        "Breaking news from Middle-Earth", "https://example.com/1", "https://example.com/image1.jpg", Instant.now(), "Unexpected visitors in the Shire."),
                new ArticleDTO(new SourceDTO("shire-times", "Shire Times"), "Frodo", "Adventure in Mordor",
                        "Journey to Mount Doom", "https://example.com/2", "https://example.com/image2.jpg", Instant.now(), "The journey begins.")
        );

        when(newsExternalServiceIntegration.fetchTopHeadlines(params)).thenReturn(articleDTOs);
        when(articleRepository.findAllUrls()).thenReturn(List.of("https://example.com/1"));
        when(articleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<ArticleDTO> result = newsService.fetchAndSaveTopHeadlines(params);

        assertEquals(1, result.size());
        assertEquals("Adventure in Mordor", result.get(0).title());
        verify(articleRepository, times(1)).saveAll(anyList());
    }

    @Test
    void shouldFetchAndSaveUniqueSources() {
        List<SourceDTO> sourceDTOs = List.of(
                new SourceDTO("middle-earth", "Middle-Earth Chronicle", "News from Middle-Earth", "https://middle-earth.com", "fantasy", "en", "ME"),
                new SourceDTO("shire-times", "Shire Times", "News from the Shire", "https://shire-times.com", "fantasy", "en", "ME"),
                new SourceDTO("gondor-gazette", "Gondor Gazette", "Updates from Gondor", "https://gondor.com", "fantasy", "en", "ME"),
                new SourceDTO("rohan-report", "Rohan Report", "News from Rohan", "https://rohan.com", "fantasy", "en", "ME")
        );

        when(newsExternalServiceIntegration.fetchSources()).thenReturn(sourceDTOs);
        when(sourceRepository.findAllSourcesId()).thenReturn(List.of("middle-earth"));
        when(sourceRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SourceDTO> result = newsService.fetchAndSaveSources();

        assertEquals(3, result.size());
        assertEquals("Shire Times", result.get(0).name());
        assertEquals("Gondor Gazette", result.get(1).name());
        assertEquals("Rohan Report", result.get(2).name());
        verify(sourceRepository, times(1)).saveAll(anyList());
    }

    @Test
    void shouldReturnPaginatedNews() {
        List<Article> articles = createSampleArticles();

        when(articleRepository.findArticlesWithLimitAndOffset(2, 0)).thenReturn(articles);

        List<Article> result = newsService.getNews(2, 0);

        assertEquals(2, result.size());
        assertEquals("Penguins Invade Middle-Earth", result.get(0).getTitle());
        assertEquals("Adventure in Mordor", result.get(1).getTitle());
    }

    private List<Article> createSampleArticles() {
        Article article1 = new Article();
        article1.setSourceName("middle-earth");
        article1.setAuthor("Gandalf");
        article1.setTitle("Penguins Invade Middle-Earth");
        article1.setDescription("Breaking news from Middle-Earth");
        article1.setUrl("https://example.com/1");
        article1.setUrlToImage("https://example.com/image1.jpg");
        article1.setPublishedAt(Instant.now());
        article1.setContent("Unexpected visitors in the Shire.");

        Article article2 = new Article();
        article2.setSourceName("shire-times");
        article2.setAuthor("Frodo");
        article2.setTitle("Adventure in Mordor");
        article2.setDescription("Journey to Mount Doom");
        article2.setUrl("https://example.com/2");
        article2.setUrlToImage("https://example.com/image2.jpg");
        article2.setPublishedAt(Instant.now());
        article2.setContent("The journey begins.");

        return List.of(article1, article2);
    }
}
