package com.interview.news.api.service;

import com.interview.news.domain.model.dto.ArticleDTO;
import com.interview.news.domain.model.dto.ArticleParamsDTO;
import com.interview.news.domain.model.dto.SourceDTO;
import com.interview.news.domain.model.entity.Source;
import com.interview.news.persistance.SourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class NewsSchedulerPerformanceTest {

    @Mock
    private SourceRepository sourceRepository;

    @Mock
    private NewsService newsService;

    @InjectMocks
    private NewsScheduler newsScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        prepareMockedNewsServiceResponse();
    }

    @Test
    void shouldFetchArticlesBySourcesConcurrently() {
        List<Source> mockSources = createMockSources(100000);
        when(sourceRepository.findAll()).thenReturn(mockSources);

        long startTime = System.currentTimeMillis();
        newsScheduler.fetchArticlesBySourcesConcurrent();
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("Execution time (Concurrently): " + duration + " ms");
        Mockito.verify(newsService, times(100000)).fetchAndSaveTopHeadlines(any(ArticleParamsDTO.class));
    }

    @Test
    void shouldFetchArticlesBySourcesSequentially() {
        List<Source> mockSources = createMockSources(1000000);
        when(sourceRepository.findAll()).thenReturn(mockSources);

        long startTime = System.currentTimeMillis();
        newsScheduler.fetchArticlesBySourcesSequential();
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("Execution time (sequentially): " + duration + " ms");
        Mockito.verify(newsService, times(1000000)).fetchAndSaveTopHeadlines(any(ArticleParamsDTO.class));
    }

    private List<Source> createMockSources(int count) {
        List<Source> sources = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Source source = new Source();
            source.setSourceId("source-" + i);
            source.setName("Source " + i);
            sources.add(source);
        }
        return sources;
    }

    private void prepareMockedNewsServiceResponse() {
        SourceDTO mockSource = new SourceDTO("mock-source-id", "Mock Source");
        ArticleDTO mockArticle = new ArticleDTO(
                mockSource,
                "Mock Author",
                "Mock Title",
                "Mock Description",
                "http://mock-url.com/article",
                "http://mock-url.com/image.jpg",
                Instant.now(),
                "Mock Content"
        );

        when(newsService.fetchAndSaveTopHeadlines(any(ArticleParamsDTO.class)))
                .thenReturn(List.of(mockArticle));
    }
}
