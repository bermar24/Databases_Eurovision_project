package com.dhbw.eurovision;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test — verifies the Spring application context loads correctly.
 * Requires a running MySQL instance (or H2 in-memory for CI).
 *
 * To run without a real DB, add the h2 dependency to pom.xml and
 * set spring.datasource.url=jdbc:h2:mem:testdb in test/resources/application.properties
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class EurovisionApplicationTests {

    @Test
    void contextLoads() {
        // If this passes, all beans wired up correctly
    }
}
