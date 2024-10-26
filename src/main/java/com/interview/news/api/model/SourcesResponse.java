package com.interview.news.api.model;

import com.interview.news.domain.model.dto.SourceDTO;

import java.util.List;

public record SourcesResponse(String status, List<SourceDTO> sources) {
}
