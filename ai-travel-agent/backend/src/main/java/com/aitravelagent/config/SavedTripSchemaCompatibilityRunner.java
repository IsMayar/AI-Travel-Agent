package com.aitravelagent.config;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SavedTripSchemaCompatibilityRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SavedTripSchemaCompatibilityRunner.class);
    private static final String SAVED_TRIPS_TABLE = "saved_trips";

    private final JdbcTemplate jdbcTemplate;

    public SavedTripSchemaCompatibilityRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!tableExists(SAVED_TRIPS_TABLE)) {
            return;
        }

        addColumnIfMissing("favorite", "BOOLEAN DEFAULT FALSE");
        addColumnIfMissing("created_at", "TIMESTAMP WITH TIME ZONE");
        addColumnIfMissing("updated_at", "TIMESTAMP WITH TIME ZONE");

        jdbcTemplate.update("UPDATE saved_trips SET favorite = FALSE WHERE favorite IS NULL");
        jdbcTemplate.update("UPDATE saved_trips SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL");
        jdbcTemplate.update("""
                UPDATE saved_trips
                SET updated_at = COALESCE(updated_at, created_at, CURRENT_TIMESTAMP)
                WHERE updated_at IS NULL
                """);

        executeIfPossible("ALTER TABLE saved_trips ALTER COLUMN favorite SET DEFAULT FALSE");
    }

    private void addColumnIfMissing(String columnName, String columnDefinition) {
        if (!columnExists(SAVED_TRIPS_TABLE, columnName)) {
            jdbcTemplate.execute("ALTER TABLE saved_trips ADD COLUMN " + columnName + " " + columnDefinition);
        }
    }

    private boolean tableExists(String tableName) {
        Boolean exists = jdbcTemplate.execute((ConnectionCallback<Boolean>) connection -> {
            DatabaseMetaData metadata = connection.getMetaData();
            return hasTable(metadata, tableName) || hasTable(metadata, tableName.toUpperCase());
        });

        return Boolean.TRUE.equals(exists);
    }

    private boolean columnExists(String tableName, String columnName) {
        Boolean exists = jdbcTemplate.execute((ConnectionCallback<Boolean>) connection -> {
            DatabaseMetaData metadata = connection.getMetaData();
            return hasColumn(metadata, tableName, columnName)
                    || hasColumn(metadata, tableName.toUpperCase(), columnName.toUpperCase());
        });

        return Boolean.TRUE.equals(exists);
    }

    private boolean hasTable(DatabaseMetaData metadata, String tableName) throws SQLException {
        try (ResultSet tables = metadata.getTables(null, null, tableName, new String[] { "TABLE" })) {
            return tables.next();
        }
    }

    private boolean hasColumn(DatabaseMetaData metadata, String tableName, String columnName) throws SQLException {
        try (ResultSet columns = metadata.getColumns(null, null, tableName, columnName)) {
            return columns.next();
        }
    }

    private void executeIfPossible(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException exception) {
            LOGGER.warn("Could not apply optional saved_trips schema compatibility SQL: {}", sql, exception);
        }
    }
}
