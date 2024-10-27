package com.interview.news.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.news.api.exception.ExternalBadRequestException;
import com.interview.news.api.exception.ExternalClientUnknownException;
import com.interview.news.api.exception.ExternalNotFoundException;
import com.interview.news.api.exception.ExternalRateLimitExceededException;
import com.interview.news.api.exception.ExternalServerErrorException;
import com.interview.news.api.exception.ExternalUnauthorizedException;
import com.interview.news.api.model.NewsApiResponseError;
import com.interview.news.api.model.SourcesResponse;
import com.interview.news.api.model.TopHeadlinesResponse;
import com.interview.news.domain.model.dto.ArticleDTO;
import com.interview.news.domain.model.dto.ArticleParamsDTO;
import com.interview.news.domain.model.dto.SourceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
@Service
public class NewsExternalServiceIntegration {

    private static final String API_KEY = "b143d95790ab4f80aed6f66ea3b653b8";
    private static final String NEWS_API_URL = "https://newsapi.org/v2/top-headlines";
    private static final String SOURCES_API_URL = "https://newsapi.org/v2/sources";
    private static final Logger LOGGER = Logger.getLogger(NewsExternalServiceIntegration.class.getName());
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public NewsExternalServiceIntegration(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<ArticleDTO> fetchTopHeadlines(final ArticleParamsDTO articleParams) {
        String url = buildUrlForHeadlines(articleParams);
        LOGGER.info(() -> "Starting fetchTopHeadlines with URL: " + url);

        try {
            TopHeadlinesResponse response = restTemplate.getForObject(url, TopHeadlinesResponse.class);
            LOGGER.info(() -> "Successfully fetched top headlines");
            return getArticlesFromResponse(response);
        } catch (HttpClientErrorException e) {
            LOGGER.warning(() -> "Client error while fetching top headlines: " + e.getMessage());
            handleClientError(e);
        } catch (HttpServerErrorException e) {
            LOGGER.warning(() -> "Server error while fetching top headlines: " + e.getMessage());
            handleServerError(e);
        } catch (RestClientException e) {
            LOGGER.severe(() -> "Connection error while calling News API: " + e.getMessage());
            throw new ExternalClientUnknownException("Error while calling News API", e);
        }

        return Collections.emptyList();
    }

    public List<SourceDTO> fetchSources() {
        String url = UriComponentsBuilder.fromHttpUrl(SOURCES_API_URL)
                .queryParam("apiKey", API_KEY)
                .toUriString();
        LOGGER.info(() -> "Starting fetchSources with URL: " + url);

        try {
            SourcesResponse response = restTemplate.getForObject(url, SourcesResponse.class);
            LOGGER.info("Successfully fetched sources");
            return response != null ? response.sources() : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            LOGGER.warning("Client error while fetching sources: " + e.getMessage());
            handleClientError(e);
        } catch (HttpServerErrorException e) {
            LOGGER.warning("Server error while fetching sources: " + e.getMessage());
            handleServerError(e);
        } catch (RestClientException e) {
            LOGGER.severe("Connection error while calling News API: " + e.getMessage());
            throw new ExternalClientUnknownException("Error while calling News API", e);
        }

        return Collections.emptyList();
    }

    private String buildUrlForHeadlines(final ArticleParamsDTO articleParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(NEWS_API_URL)
                .queryParam("apiKey", API_KEY);

        if (articleParams.country() != null) {
            builder.queryParam("country", articleParams.country());
        }
        if (articleParams.category() != null) {
            builder.queryParam("category", articleParams.category());
        }
        if (articleParams.sources() != null) {
            builder.queryParam("sources", articleParams.sources());
        }

        String finalUrl = builder.toUriString();
        LOGGER.info(() -> "Built URL for headlines: " + finalUrl);
        return finalUrl;
    }

    private List<ArticleDTO> getArticlesFromResponse(TopHeadlinesResponse response) {
        if (response == null || response.articles() == null) {
            LOGGER.warning("Empty response or no articles found");
            return Collections.emptyList();
        }
        LOGGER.info(() -> "Fetched " + response.articles().size() + " articles");
        return response.articles();
    }

    private void handleClientError(HttpClientErrorException e) {
        LOGGER.warning("Handling client error: " + e.getMessage());
        NewsApiResponseError errorResponse = parseErrorResponse(e);
        String errorCode = errorResponse.code();
        String errorMessage = errorResponse.message();

        LOGGER.warning(() -> "Mapped client error with code: " + errorCode + ", message: " + errorMessage);
        throw mapClientErrorToException(errorCode, errorMessage);
    }

    private NewsApiResponseError parseErrorResponse(HttpClientErrorException e) {
        try {
            LOGGER.fine("Parsing error response: " + e.getResponseBodyAsString());
            return objectMapper.readValue(e.getResponseBodyAsString(), NewsApiResponseError.class);
        } catch (JsonProcessingException ex) {
            LOGGER.severe("Failed to parse error response: " + ex.getMessage());
            throw new RuntimeException("Error parsing error response: " + ex.getMessage(), ex);
        }
    }

    private RuntimeException mapClientErrorToException(String errorCode, String errorMessage) {
        return switch (errorCode) {
            case "apiKeyDisabled", "apiKeyExhausted", "apiKeyInvalid", "apiKeyMissing" -> {
                LOGGER.warning(() -> "Unauthorized error with code: " + errorCode);
                yield new ExternalUnauthorizedException(errorMessage);
            }
            case "parameterInvalid", "parametersMissing", "sourcesTooMany" -> {
                LOGGER.warning(() -> "Bad request error with code: " + errorCode);
                yield new ExternalBadRequestException(errorMessage);
            }
            case "rateLimited" -> {
                LOGGER.warning(() -> "Rate limit error with code: " + errorCode);
                yield new ExternalRateLimitExceededException(errorMessage);
            }
            case "sourceDoesNotExist" -> {
                LOGGER.warning(() -> "Not found error with code: " + errorCode);
                yield new ExternalNotFoundException(errorMessage);
            }
            case "unexpectedError" -> {
                LOGGER.warning(() -> "Server error with code: " + errorCode);
                yield new ExternalServerErrorException(errorMessage);
            }
            default -> {
                LOGGER.warning(() -> "Unhandled client error with code: " + errorCode);
                yield new ExternalBadRequestException("Unexpected Error: " + errorMessage);
            }
        };
    }

    private void handleServerError(HttpServerErrorException e) {
        HttpStatusCode statusCode = e.getStatusCode();
        LOGGER.warning(() -> "Handling server error with status code: " + statusCode);

        if (statusCode.equals(INTERNAL_SERVER_ERROR)) {
            throw new ExternalServerErrorException("Internal Server Error: Please try again later.");
        } else if (statusCode.equals(BAD_GATEWAY)) {
            throw new ExternalServerErrorException("Bad Gateway: Issue with upstream server.");
        } else if (statusCode.equals(SERVICE_UNAVAILABLE)) {
            throw new ExternalServerErrorException("Service Unavailable: NewsAPI is down.");
        }

        throw new ExternalServerErrorException("Unexpected Server Error: " + e.getMessage());
    }
}
