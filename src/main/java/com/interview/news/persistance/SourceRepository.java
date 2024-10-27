package com.interview.news.persistance;

import com.interview.news.domain.model.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {

    @Query("SELECT s.sourceId FROM Source s")
    List<String> findAllSourcesId();
}