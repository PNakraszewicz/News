package com.interview.news.persistance;

import com.interview.news.domain.model.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query(value = "SELECT * FROM article ORDER BY published_at DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Article> findArticlesWithLimitAndOffset(@Param("limit") int limit, @Param("offset") int offset);

    boolean existsByUrl(String url);

}
