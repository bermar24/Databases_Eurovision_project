package com.dhbw.eurovision;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/**
 * Smoke test — verifies the Spring application context loads correctly.
 * Uses an embedded H2 database so the suite runs without MySQL.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
class EurovisionApplicationTests {

    @Test
    void contextLoads() {
        // If this passes, all beans wired up correctly
    }

    @TestConfiguration
    static class TestDataSourceConfig {

        @Bean
        @Primary
        DataSource testDataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("eurovision_testdb")
                    .build();
        }
    }
}
