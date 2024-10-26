package com.interview.news.persistance;

import com.interview.news.BaseDatabaseTest;
import com.interview.news.domain.model.entity.Source;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SourceRepositoryTest extends BaseDatabaseTest {

    @Autowired
    private SourceRepository sourceRepository;

    @Test
    void shouldSaveSource() {
        final Source source = new Source();
        source.setSourceId("zoo-tv");
        source.setName("ZOO TV");
        source.setDescription("TV from NewYork zoo");
        source.setUrl("https://zoo-tv.com");
        source.setCategory("nature");
        source.setLanguage("en");
        source.setCountry("US");

        final Source savedSource = sourceRepository.save(source);

        assertNotNull(savedSource.getId());
        assertEquals("zoo-tv", savedSource.getSourceId());
        assertEquals("ZOO TV", savedSource.getName());
        assertEquals("TV from NewYork zoo", savedSource.getDescription());
        assertEquals("https://zoo-tv.com", savedSource.getUrl());
        assertEquals("nature", savedSource.getCategory());
        assertEquals("en", savedSource.getLanguage());
        assertEquals("US", savedSource.getCountry());
    }

    @Test
    void shouldReturnTrueWhenSourceExistsBySourceId() {
        final Source source = new Source();
        source.setSourceId("middle-earth-chronicle");
        source.setName("Middle-Earth Chronicle");
        source.setDescription("Middle-Earth Chronicle desc");
        source.setUrl("https://middle-earth-chronicle.com");
        source.setCategory("fantasy");
        source.setLanguage("en");
        source.setCountry("ME");

        sourceRepository.save(source);

        assertTrue(sourceRepository.existsBySourceId("middle-earth-chronicle"));
    }
}
