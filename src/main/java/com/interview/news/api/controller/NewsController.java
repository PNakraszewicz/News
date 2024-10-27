package com.interview.news.api.controller;

import com.interview.news.api.service.NewsService;
import com.interview.news.domain.model.dto.ArticleDTO;
import com.interview.news.domain.model.dto.ArticleParamsDTO;
import com.interview.news.domain.model.entity.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private static final Logger LOGGER = Logger.getLogger(NewsController.class.getName());

    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * Endpoint that fetches news articles from the News API and saves them to the database.
     * The transaction ensures that if fetching or saving fails, no incomplete data is saved.
     */
    @PostMapping("/fetch")
    public ResponseEntity<?> fetchAndSaveTopHeadlines(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sources) {

        LOGGER.info(() -> "Starting fetchAndSaveTopHeadlines with parameters: " +
                "country=" + country + ", category=" + category + ", sources=" + sources);

        try {
            ArticleParamsDTO params = new ArticleParamsDTO(country, category, sources);
            List<ArticleDTO> savedArticles = newsService.fetchAndSaveTopHeadlines(params);
            LOGGER.info(() -> "Completed fetchAndSaveTopHeadlines with " + savedArticles.size() + " articles saved.");
            return ResponseEntity.ok(savedArticles);
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Invalid parameters: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint that retrieves news articles from the database.
     * Supports optional limit and offset parameters, and results are sorted by date in descending order.
     */
    @GetMapping
    public ResponseEntity<List<Article>> getNews(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        LOGGER.info(() -> "Starting getNews with limit=" + limit + " and offset=" + offset);
        List<Article> articles = newsService.getNews(limit, offset);

        LOGGER.info(() -> "Completed getNews, retrieved " + articles.size() + " articles.");
        return ResponseEntity.ok(articles);
    }
}
