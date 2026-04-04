package backend.fullstack.checklist.integration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class FlywayChecklistMigrationIntegrationTest {

    @Test
    void flywayMigrationCreatesChecklistTables() throws Exception {
        DataSource dataSource = createDataSource();
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();

        assertTrue(v1MigrationWasApplied(dataSource), "Flyway migration V1 must be applied");
        assertTrue(tableExists(dataSource, "checklist_templates"));
        assertTrue(tableExists(dataSource, "checklist_template_items"));
        assertTrue(tableExists(dataSource, "checklist_instances"));
        assertTrue(tableExists(dataSource, "checklist_instance_items"));
    }

    private DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:flyway-checklist-it;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private boolean v1MigrationWasApplied(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND success = TRUE"
             )) {
            result.next();
            return result.getInt(1) > 0;
        }
    }

    private boolean tableExists(DataSource dataSource, String tableName) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = UPPER(?)"
             )) {
            statement.setString(1, tableName);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0;
            }
        }
    }
}
