package backend.fullstack.readings.integration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class FlywayReadingsMigrationIntegrationTest {

    @Test
    void flywayMigrationCreatesTemperatureReadingsTable() throws Exception {
        DataSource dataSource = createDataSource();
        createPrerequisiteTables(dataSource);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
        flyway.migrate();

        assertTrue(v3MigrationWasApplied(dataSource), "Flyway migration V3 must be applied");
        assertTrue(tableExists(dataSource, "temperature_readings"), "temperature_readings table must exist");
    }

    private DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:flyway-readings-it;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    /**
     * Creates stub tables referenced by FK constraints in V3 migration.
     * V3 references organizations(id), users(id), and temperature_units(id).
     * These are created outside Flyway so they exist before V3 runs.
     */
    private void createPrerequisiteTables(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS organizations (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT
                    )
                    """);
        }
    }

    private boolean v3MigrationWasApplied(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '3' AND success = TRUE"
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
