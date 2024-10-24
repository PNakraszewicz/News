package com.interview.news.api.model;

import com.interview.news.domain.model.dto.ArticleDTO;

import java.util.List;

public record TopHeadlinesResponse(String status, int totalResults, List<ArticleDTO> articles) {
}
