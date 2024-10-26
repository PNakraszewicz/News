package com.interview.news.persistance;

import com.interview.news.domain.model.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {

    boolean existsBySourceId(String sourceId);
}