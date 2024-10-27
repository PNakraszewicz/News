package com.interview.news.persistance;

import com.interview.news.BaseDatabaseTest;
import com.interview.news.domain.model.entity.Source;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

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
    void shouldReturnAllSourceIds() {
        // Given
        Source source1 = new Source();
        source1.setSourceId("zoo-tv");
        source1.setName("ZOO TV");
        source1.setDescription("TV from New York zoo");
        source1.setUrl("https://zoo-tv.com");
        source1.setCategory("nature");
        source1.setLanguage("en");
        source1.setCountry("US");

        Source source2 = new Source();
        source2.setSourceId("middle-earth-chronicle");
        source2.setName("Middle-Earth Chronicle");
        source2.setDescription("News from Middle-Earth");
        source2.setUrl("https://middle-earth.com");
        source2.setCategory("fantasy");
        source2.setLanguage("en");
        source2.setCountry("ME");

        sourceRepository.save(source1);
        sourceRepository.save(source2);

        // When
        List<String> sourceIds = sourceRepository.findAllSourcesId();

        // Then
        assertNotNull(sourceIds);
        assertEquals(2, sourceIds.size());
        assertTrue(sourceIds.contains("zoo-tv"));
        assertTrue(sourceIds.contains("middle-earth-chronicle"));
    }
}
