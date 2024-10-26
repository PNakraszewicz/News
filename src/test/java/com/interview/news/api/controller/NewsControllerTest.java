package com.interview.news.api.controller;

import com.interview.news.BaseDatabaseTest;
import com.interview.news.api.exception.ExternalUnauthorizedException;
import com.interview.news.api.service.NewsExternalServiceIntegration;
import com.interview.news.domain.model.dto.ArticleDTO;
import com.interview.news.domain.model.dto.ArticleParamsDTO;
import com.interview.news.domain.model.dto.SourceDTO;
import com.interview.news.domain.model.entity.Article;
import com.interview.news.persistance.ArticleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class NewsControllerTest extends BaseDatabaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @MockBean
    private NewsExternalServiceIntegration newsExternalServiceIntegration;

    @AfterEach
    void cleanDatabase() {
        articleRepository.deleteAll();
    }

    @Test
    void shouldFetchAndSaveArticlesToDatabase() throws Exception {
        prepareMockedResponse();
        mockMvc.perform(post("/api/news/fetch")
                        .param("country", "us")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Penguins Found on Madagascar"))
                .andExpect(jsonPath("$[0].source.name").value("ZOO TV"))
                .andExpect(jsonPath("$[1].title").value("Penguins Invade Middle-Earth"))
                .andExpect(jsonPath("$[1].source.name").value("Middle-Earth Chronicle"));

        List<Article> articlesInDb = articleRepository.findAll();

        assertEquals(2, articlesInDb.size(), "Should have two articles in the database");
        assertTrue(
                articlesInDb.stream().anyMatch(article -> "Penguins Found on Madagascar".equals(article.getTitle())),
                "Database should contain article with title 'Penguins Found on Madagascar'"
        );
        assertTrue(
                articlesInDb.stream().anyMatch(article -> "Penguins Invade Middle-Earth".equals(article.getTitle())),
                "Database should contain article with title 'Penguins Invade Middle-Earth'"
        );
    }

    @Test
    void shouldReturnUnauthorizedForInvalidApiKey() throws Exception {
        Mockito.when(newsExternalServiceIntegration.fetchTopHeadlines(any(ArticleParamsDTO.class)))
                .thenThrow(new ExternalUnauthorizedException("Invalid API Key"));

        mockMvc.perform(post("/api/news/fetch")
                        .param("country", "us")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        assertTrue(articleRepository.findAll().isEmpty(), "Database should remain empty on failure");
    }


    @Test
    void shouldReturnLimitedNumberOfArticlesWithOffset() throws Exception {
        prepareDatabaseWithMultipleArticles(10);
        int limit = 5;
        int offset = 4;

        mockMvc.perform(get("/api/news")
                        .param("limit", String.valueOf(limit))
                        .param("offset", String.valueOf(offset))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(limit)))
                .andExpect(jsonPath("$[0].title").value("Article Title 6"))
                .andExpect(jsonPath("$[1].title").value("Article Title 5"))
                .andExpect(jsonPath("$[2].title").value("Article Title 4"))
                .andExpect(jsonPath("$[3].title").value("Article Title 3"))
                .andExpect(jsonPath("$[4].title").value("Article Title 2"));
    }

    @Test
    void shouldFetchAndSaveArticlesWhenOnlyCountryIsUsed() throws Exception {
        prepareMockedResponse();

        mockMvc.perform(post("/api/news/fetch")
                        .param("country", "us")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Penguins Found on Madagascar"))
                .andExpect(jsonPath("$[0].source.name").value("ZOO TV"))
                .andExpect(jsonPath("$[1].title").value("Penguins Invade Middle-Earth"))
                .andExpect(jsonPath("$[1].source.name").value("Middle-Earth Chronicle"));
    }

    @Test
    void shouldFetchAndSaveArticlesWhenOnlyCategoryIsUsed() throws Exception {
        prepareMockedResponse();

        mockMvc.perform(post("/api/news/fetch")
                        .param("category", "technology")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Penguins Found on Madagascar"))
                .andExpect(jsonPath("$[0].source.name").value("ZOO TV"))
                .andExpect(jsonPath("$[1].title").value("Penguins Invade Middle-Earth"))
                .andExpect(jsonPath("$[1].source.name").value("Middle-Earth Chronicle"));
    }

    @Test
    void shouldReturnBadRequestWhenCountryAndCategoryAreUsedTogether() throws Exception {
        mockMvc.perform(post("/api/news/fetch")
                        .param("country", "us")
                        .param("category", "technology")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Source param cannot be mixed with other params"));
    }

    @Test
    void shouldReturnBadRequestWhenCountryAndSourcesAreUsedTogether() throws Exception {
        mockMvc.perform(post("/api/news/fetch")
                        .param("country", "us")
                        .param("sources", "bbc-news")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Source param cannot be mixed with other params"));
    }

    @Test
    void shouldReturnBadRequestWhenCategoryAndSourcesAreUsedTogether() throws Exception {
        mockMvc.perform(post("/api/news/fetch")
                        .param("category", "technology")
                        .param("sources", "bbc-news")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Source param cannot be mixed with other params"));
    }

    private void prepareMockedResponse() {
        SourceDTO source1 = new SourceDTO("zoo-tv", "ZOO TV");
        SourceDTO source2 = new SourceDTO("middle-earth-chronicle", "Middle-Earth Chronicle");

        List<ArticleDTO> articles = List.of(
                new ArticleDTO(
                        source1,
                        "King Julian",
                        "Penguins Found on Madagascar",
                        "Breaking news about penguins and their adventures on the island of Madagascar",
                        "http://zoo-tv.com/penguins-on-madagascar",
                        "http://zoo-tv.com/images/penguins.jpg",
                        Instant.parse("2024-10-24T10:00:00Z"),
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                ),
                new ArticleDTO(
                        source2,
                        "Gandalf the Grey",
                        "Penguins Invade Middle-Earth",
                        "Unusual visitors have arrived in Middle-Earth, surprising both hobbits and wizards alike",
                        "http://middle-earth-chronicle.com/penguins-invade",
                        "http://middle-earth-chronicle.com/images/penguins-middle-earth.jpg",
                        Instant.parse("2024-10-24T11:00:00Z"),
                        "The citizens of Middle-Earth were astonished as a group of penguins appeared in the Shire."
                )
        );

        Mockito.when(newsExternalServiceIntegration.fetchTopHeadlines(any(ArticleParamsDTO.class))).thenReturn(articles);
    }

    private void prepareDatabaseWithMultipleArticles(final int count) {
        List<Article> articles = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Article article = new Article();
            article.setSourceName("Source " + i);
            article.setAuthor("Author " + i);
            article.setTitle("Article Title " + i);
            article.setDescription("Description for article " + i);
            article.setUrl("http://example.com/article" + i);
            article.setUrlToImage("http://example.com/image" + i + ".jpg");
            article.setPublishedAt(Instant.parse("2024-10-24T10:00:00Z").plusSeconds(i * 60));
            article.setContent("Content for article " + i);
            articles.add(article);
        }
        articleRepository.saveAll(articles);
    }

}