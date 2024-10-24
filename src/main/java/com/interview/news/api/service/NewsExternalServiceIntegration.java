package com.interview.news.api.service;

import com.interview.news.api.model.TopHeadlinesResponse;
import com.interview.news.domain.model.dto.ArticleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class NewsExternalServiceIntegration {

    private static final String API_KEY = "b143d95790ab4f80aed6f66ea3b653b8";
    private static final String NEWS_API_URL = "https://newsapi.org/v2/top-headlines";

    private final RestTemplate restTemplate;

    @Autowired
    public NewsExternalServiceIntegration(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<ArticleDTO> fetchTopHeadlines(String country) {
        String url = UriComponentsBuilder.fromHttpUrl(NEWS_API_URL)
                .queryParam("country", country)
                .queryParam("apiKey", API_KEY)
                .toUriString();

        TopHeadlinesResponse response = restTemplate.getForObject(url, TopHeadlinesResponse.class);

        return response != null && response.articles() != null ? response.articles() : List.of();
    }
}
