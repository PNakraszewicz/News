package com.interview.news.persistance;

import com.interview.news.BaseDatabaseTest;
import com.interview.news.domain.model.entity.Article;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArticleRepositoryTest extends BaseDatabaseTest {

    @Autowired
    private ArticleRepository articleRepository;

    @AfterEach
    void cleanUp() {
        articleRepository.deleteAll();
    }

    @Test
    void shouldSaveArticle() {
        final Article article = createTestData("https://some-s3-url/article");

        final Article savedArticle = articleRepository.save(article);

        assertNotNull(savedArticle.getId());
        assertEquals("Penguins found on the Madagascar", savedArticle.getTitle());
        assertEquals("ZOO TV", savedArticle.getSourceName());
        assertEquals("King Julian", savedArticle.getAuthor());
        assertEquals("Breaking news about animals from the New York zoo", savedArticle.getDescription());
        assertEquals("https://some-s3-url/article", savedArticle.getUrl());
        assertEquals("https://some-s3-url/image.jpg", savedArticle.getUrlToImage());
        assertNotNull(savedArticle.getPublishedAt());
        assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque luctus diam sit amet lectus iaculis pellentesque. Maecenas eget enim lectus. Ut semper dolor est. Sed mauris justo, convallis sit amet massa sit amet, placerat lacinia neque. Nunc fringilla dapibus erat sed facilisis. Aenean lacinia magna a augue congue, vitae vehicula massa rutrum. Duis nisl metus, congue sit amet mi in, fringilla gravida metus. Aliquam convallis dapibus nulla eget finibus. Sed dictum, leo vitae lacinia amet.", savedArticle.getContent());
    }

    @Test
    void shouldReturnTrueWhenArticleExistsByUrl() {
        final Article article = createTestData("https://some-s3-url/article");
        articleRepository.save(article);

        boolean exists = articleRepository.existsByUrl("https://some-s3-url/article");

        assertTrue(exists, "Article should exist by URL");
    }

    private Article createTestData(final String url) {
        final Article article = new Article();
        article.setSourceName("ZOO TV");
        article.setAuthor("King Julian");
        article.setTitle("Penguins found on the Madagascar");
        article.setDescription("Breaking news about animals from the New York zoo");
        article.setUrl(url);
        article.setUrlToImage("https://some-s3-url/image.jpg");
        article.setPublishedAt(Instant.now());
        article.setContent("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque luctus diam sit amet lectus iaculis pellentesque. Maecenas eget enim lectus. Ut semper dolor est. Sed mauris justo, convallis sit amet massa sit amet, placerat lacinia neque. Nunc fringilla dapibus erat sed facilisis. Aenean lacinia magna a augue congue, vitae vehicula massa rutrum. Duis nisl metus, congue sit amet mi in, fringilla gravida metus. Aliquam convallis dapibus nulla eget finibus. Sed dictum, leo vitae lacinia amet.");
        return article;
    }
}
