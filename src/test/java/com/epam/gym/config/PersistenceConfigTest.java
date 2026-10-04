package com.epam.gym.config;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
@TestPropertySource("classpath:application-test.properties")
class PersistenceConfigTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void persistenceBeansAreCreated() {
        assertNotNull(dataSource);
        assertNotNull(entityManagerFactory);
        assertNotNull(transactionManager);
    }

    @Test
    void schemaScriptCreatesAllTables() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        for (String table : List.of("users", "trainees", "trainers", "training_types", "trainings", "trainee_trainer")) {
            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
            assertNotNull(count, "Table " + table + " should exist");
        }
    }

    @Test
    void dataScriptInsertsTrainingTypes() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        List<String> names = jdbc.queryForList(
                "SELECT training_type_name FROM training_types ORDER BY training_type_name", String.class);

        assertEquals(6, names.size());
        assertTrue(names.contains("Yoga"));
        assertTrue(names.contains("Cardio"));
    }
}
