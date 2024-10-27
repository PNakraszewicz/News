
# News API Integration
This project is a Java-based News Aggregator application developed using Spring Boot. 
It integrates with the News API to fetch, store, and manage news articles from various sources. 
The application provides endpoints for fetching top news headlines, retrieving articles from the database, and managing sources.

## Features
News API Integration: Connects with the News API to fetch and process news headlines.
Database Storage: Stores news articles and sources in a PostgreSQL database.
RESTful Endpoints: Offers endpoints for fetching and retrieving news articles.
Transaction Management: Ensures data consistency using transactions.
Error Handling: Handles various error cases including API rate limits, invalid API keys, and other HTTP errors.
Caching and Scheduling: Reduces redundant API calls using caching and automatically fetches updates on a scheduled interval.
Concurrency: Fetches news articles concurrently for improved performance.

## Tech Stack
### Java 17
### Spring Boot (Spring Data JPA, Spring Web, Spring Cache)
### PostgreSQL for data storage
### Testcontainers for integration testing
### Docker for containerized testing and deployment

## Prerequisites
### Docker 
Ensure that Docker is installed and running.
### Available Port 5432: 
PostgreSQL will run on port 5432, so ensure that this port is available.
### Maven: 
Maven is required to build and run the application.

## Getting Started
Build the application:
```bash
mvn clean install
```
Run the application:
Run as configuration NewsApplication class
or
```bash
mvn spring-boot:run
```