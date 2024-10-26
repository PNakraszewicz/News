package com.interview.news.api.service;

import com.interview.news.domain.model.dto.ArticleParamsDTO;
import com.interview.news.domain.model.entity.Source;
import com.interview.news.persistance.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NewsScheduler {

    private final SourceRepository sourceRepository;
    private final NewsService newsService;

    @Autowired
    public NewsScheduler(SourceRepository sourceRepository, NewsService newsService) {
        this.sourceRepository = sourceRepository;
        this.newsService = newsService;
    }

    public void fetchArticlesBySourcesConcurrent() {
        List<Source> uniqueSources = sourceRepository.findAll();

        uniqueSources.parallelStream().forEach(source -> {
            ArticleParamsDTO params = new ArticleParamsDTO(null, null, source.getName());
            newsService.fetchAndSaveTopHeadlines(params);
        });
    }

    /*
        Method left only to show the difference in performance between parallel streaming and standard for each approach.
        To observe the difference - may trigger NewsSchedulerPerformanceTest class.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void fetchArticlesBySourcesSequential() {
        List<Source> uniqueSources = sourceRepository.findAll();

        uniqueSources.forEach(source -> {
            ArticleParamsDTO params = new ArticleParamsDTO(null, null, source.getName());
            newsService.fetchAndSaveTopHeadlines(params);
        });
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void fetchSourcesPeriodically() {
        newsService.getSources();
    }
}
