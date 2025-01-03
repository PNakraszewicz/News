package com.interview.news.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.news.api.exception.ExternalBadRequestException;
import com.interview.news.api.exception.ExternalRateLimitExceededException;
import com.interview.news.api.exception.ExternalServerErrorException;
import com.interview.news.api.exception.ExternalUnauthorizedException;
import com.interview.news.api.model.SourcesResponse;
import com.interview.news.api.model.TopHeadlinesResponse;
import com.interview.news.domain.model.dto.ArticleDTO;
import com.interview.news.domain.model.dto.ArticleParamsDTO;
import com.interview.news.domain.model.dto.SourceDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class NewsExternalServiceIntegrationTest {

    @Mock
    private RestTemplate restTemplate;

    /*
        As an improvement would consider introducing WireMock as a library more feasible for mocking API interaction
    */
    @InjectMocks
    private NewsExternalServiceIntegration newsExternalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        newsExternalService = new NewsExternalServiceIntegration(restTemplate, new ObjectMapper());
    }

    @Test
    void shouldReturnListOfArticlesForValidApiKey() {
        SourceDTO source1 = new SourceDTO("zoo-tv", "ZOO TV");
        SourceDTO source2 = new SourceDTO("frodo-news", "Frodo News");

        List<ArticleDTO> articles = List.of(
                new ArticleDTO(
                        source1,
                        "King Julian",
                        "Penguins Found on Madagascar",
                        "Breaking news about penguins on the Madagascar",
                        "https://some-s3-url/article1",
                        "https://some-s3-url/image1.jpg",
                        Instant.parse("2024-10-24T10:00:00Z"),
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                ),
                new ArticleDTO(
                        source2,
                        "Gandalf the Grey",
                        "Penguins Invade Middle-Earth",
                        "Unusual visitors have arrived in Middle-Earth",
                        "https://some-s3-url/article2",
                        "https://some-s3-url/image2.jpg",
                        Instant.parse("2024-10-24T11:00:00Z"),
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                )
        );

        TopHeadlinesResponse response = new TopHeadlinesResponse("ok", 2, articles);

        when(restTemplate.getForObject(anyString(), Mockito.eq(TopHeadlinesResponse.class)))
                .thenReturn(response);

        List<ArticleDTO> result = newsExternalService.fetchTopHeadlines(new ArticleParamsDTO("us", null, null));

        assertEquals(2, result.size());
        assertEquals("Penguins Found on Madagascar", result.get(0).title());
        assertEquals("ZOO TV", result.get(0).source().name());
        assertEquals("Penguins Invade Middle-Earth", result.get(1).title());
        assertEquals("Frodo News", result.get(1).source().name());
    }

    @Test
    void shouldThrowUnauthorizedExceptionForInvalidApiKey() {
        mockClientError(
                "apiKeyInvalid",
                "Invalid API Key: Your API key is invalid or incorrect.",
                HttpStatus.UNAUTHORIZED,
                ExternalUnauthorizedException.class
        );
    }

    @Test
    void shouldThrowBadRequestExceptionForInvalidParameter() {
        mockClientError(
                "parameterInvalid",
                "Invalid Parameter: The parameter is not supported.",
                HttpStatus.BAD_REQUEST,
                ExternalBadRequestException.class
        );
    }

    @Test
    void shouldThrowRateLimitExceededExceptionWhenRateLimited() {
        mockClientError(
                "rateLimited",
                "Rate Limited: You have been rate limited. Please try again later.",
                HttpStatus.TOO_MANY_REQUESTS,
                ExternalRateLimitExceededException.class
        );
    }

    @Test
    void shouldThrowServerErrorForServiceUnavailable() {
        HttpServerErrorException exception = HttpServerErrorException.create(
                HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", null, null, null);

        when(restTemplate.getForObject(anyString(), Mockito.eq(TopHeadlinesResponse.class)))
                .thenThrow(exception);

        ExternalServerErrorException thrown = assertThrows(ExternalServerErrorException.class, () -> {
            newsExternalService.fetchTopHeadlines(new ArticleParamsDTO("us", null, null));
        });

        assertEquals("Service Unavailable: NewsAPI is down.", thrown.getMessage());
    }

    @Test
    void shouldReturnListOfSources() {
        SourceDTO source1 = new SourceDTO("zoo-tv", "ZOO TV", "TV from the zoo", "https://zoo-tv.com", "nature", "en", "US");
        SourceDTO source2 = new SourceDTO("frodo-news", "Frodo News", "News from Middle-Earth", "https://frodo-news.com", "fantasy", "en", "ME");

        List<SourceDTO> sources = List.of(source1, source2);
        SourcesResponse response = new SourcesResponse("ok", sources);

        when(restTemplate.getForObject(anyString(), Mockito.eq(SourcesResponse.class))).thenReturn(response);

        List<SourceDTO> result = newsExternalService.fetchSources();

        assertEquals(2, result.size());
        assertEquals("ZOO TV", result.get(0).name());
        assertEquals("Frodo News", result.get(1).name());
    }

    @Test
    void shouldThrowUnauthorizedExceptionForInvalidApiKeyOnFetchSources() {
        mockClientErrorOnFetchSources(
                "apiKeyInvalid",
                "Invalid API Key: Your API key is invalid or incorrect.",
                HttpStatus.UNAUTHORIZED,
                ExternalUnauthorizedException.class
        );
    }

    @Test
    void shouldThrowBadRequestExceptionForInvalidParameterOnFetchSources() {
        mockClientErrorOnFetchSources(
                "parameterInvalid",
                "Invalid Parameter: The parameter is not supported.",
                HttpStatus.BAD_REQUEST,
                ExternalBadRequestException.class
        );
    }

    @Test
    void shouldThrowRateLimitExceededExceptionWhenRateLimitedOnFetchSources() {
        mockClientErrorOnFetchSources(
                "rateLimited",
                "Rate Limited: You have been rate limited. Please try again later.",
                HttpStatus.TOO_MANY_REQUESTS,
                ExternalRateLimitExceededException.class
        );
    }

    private void mockClientError(String code, String message, HttpStatus status, Class<? extends RuntimeException> expectedException) {
        String responseBody = """
                {
                    "status": "error",
                    "code": "%s",
                    "message": "%s"
                }
                """.formatted(code, message);

        HttpClientErrorException exception = HttpClientErrorException.create(
                status, "Client Error", null, responseBody.getBytes(), null);

        when(restTemplate.getForObject(anyString(), Mockito.eq(TopHeadlinesResponse.class)))
                .thenThrow(exception);

        RuntimeException thrown = assertThrows(expectedException, () -> newsExternalService.fetchTopHeadlines(new ArticleParamsDTO("us", null, null)));
        assertEquals(message, thrown.getMessage());
    }

    private void mockClientErrorOnFetchSources(String code, String message, HttpStatus status, Class<? extends RuntimeException> expectedException) {
        String responseBody = """
                {
                    "status": "error",
                    "code": "%s",
                    "message": "%s"
                }
                """.formatted(code, message);

        HttpClientErrorException exception = HttpClientErrorException.create(
                status, "Client Error", null, responseBody.getBytes(), null);

        when(restTemplate.getForObject(anyString(), Mockito.eq(SourcesResponse.class))).thenThrow(exception);

        RuntimeException thrown = assertThrows(expectedException, () -> newsExternalService.fetchSources());
        assertEquals(message, thrown.getMessage());
    }
}