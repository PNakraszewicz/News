package com.interview.news.api.model;

public record NewsApiResponseError(String status, String code, String message) {
}