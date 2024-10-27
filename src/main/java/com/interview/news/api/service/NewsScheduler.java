package com.interview.news.api.service;

import com.interview.news.domain.model.dto.ArticleParamsDTO;
import com.interview.news.domain.model.entity.Source;
import com.interview.news.persistance.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;

@Service
public class NewsScheduler {

    private static final Logger LOGGER = Logger.getLogger(NewsScheduler.class.getName());

    private final SourceRepository sourceRepository;
    private final NewsService newsService;

    @Autowired
    public NewsScheduler(SourceRepository sourceRepository, NewsService newsService) {
        this.sourceRepository = sourceRepository;
        this.newsService = newsService;
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void fetchArticlesBySourcesConcurrent() {
        LOGGER.info("Starting fetchArticlesBySourcesConcurrent");

        List<Source> uniqueSources = sourceRepository.findAll();
        uniqueSources.parallelStream().forEach(source -> {
            ArticleParamsDTO params = new ArticleParamsDTO(null, null, source.getName());
            newsService.fetchAndSaveTopHeadlines(params);
        });

        LOGGER.info("Completed fetchArticlesBySourcesConcurrent");
    }

    /*
        Method left only to show the difference in performance between parallel streaming and sequential forEach approach.
        To observe the difference - may trigger NewsSchedulerTest class.
     */
    public void fetchArticlesBySourcesSequential() {
        LOGGER.info("Starting fetchArticlesBySourcesSequential");

        List<Source> uniqueSources = sourceRepository.findAll();
        uniqueSources.forEach(source -> {
            ArticleParamsDTO params = new ArticleParamsDTO(null, null, source.getName());
            newsService.fetchAndSaveTopHeadlines(params);
        });

        LOGGER.info("Completed fetchArticlesBySourcesSequential");
    }

    @Scheduled(cron = "0 0/10 * * * ?")
    @Transactional
    public void fetchSources() {
        LOGGER.info("Starting fetchSourcesPeriodically");

        newsService.fetchAndSaveSources();

        LOGGER.info("Completed fetchSourcesPeriodically");
    }
}