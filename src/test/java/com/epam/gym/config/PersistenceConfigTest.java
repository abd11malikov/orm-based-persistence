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
    void contextStartup_createsPersistenceBeans() {
        // given
        Object[] persistenceBeans = {dataSource, entityManagerFactory, transactionManager};

        // when
        boolean entityManagerFactoryIsOpen = entityManagerFactory.isOpen();

        // then
        for (Object bean : persistenceBeans) {
            assertNotNull(bean);
        }
        assertTrue(entityManagerFactoryIsOpen);
    }

    @Test
    void schemaScriptCreatesAllTables() {
        // given
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        List<String> expectedTables =
                List.of("users", "trainees", "trainers", "training_types", "trainings", "trainee_trainer");

        // when
        List<String> existingTables = jdbc.queryForList(
                "SELECT LOWER(table_name) FROM information_schema.tables", String.class);

        // then
        assertTrue(existingTables.containsAll(expectedTables), "Existing tables: " + existingTables);
    }

    @Test
    void dataScriptInsertsTrainingTypes() {
        // given
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        // when
        List<String> names = jdbc.queryForList(
                "SELECT training_type_name FROM training_types ORDER BY training_type_name", String.class);

        // then
        assertEquals(6, names.size());
        assertTrue(names.contains("Yoga"));
        assertTrue(names.contains("Cardio"));
    }
}
