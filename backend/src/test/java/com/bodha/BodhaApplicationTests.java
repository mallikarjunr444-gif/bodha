package com.bodha;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "bodha.jwt.secret=bodha-test-jwt-secret-key-must-be-at-least-256-bits-long-32-chars")
class BodhaApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Context loads and DataSource bean is initialized")
    void contextLoads() {
        assertNotNull(dataSource, "DataSource should be initialized by Spring Boot");
    }

    @Test
    @DisplayName("Verify live PostgreSQL connection to bodha_db")
    void testDatabaseConnection() throws Exception {
        assertNotNull(dataSource, "DataSource bean must be configured");

        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "JDBC Connection must not be null");
            assertFalse(connection.isClosed(), "JDBC Connection must be active and open");

            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT current_database(), current_user, version()")) {

                assertTrue(rs.next(), "Database query should return at least one row");

                String currentDatabase = rs.getString(1);
                String currentUser = rs.getString(2);
                String version = rs.getString(3);

                assertEquals("bodha_db", currentDatabase, "Database name must be bodha_db");
                assertNotNull(currentUser, "Current user must be present");
                assertTrue(version.contains("PostgreSQL 16"), "Database version must be PostgreSQL 16");

                System.out.println("=================================================");
                System.out.println("   BODHA POSTGRESQL CONNECTIVITY TEST: PASSED    ");
                System.out.println("   Database: " + currentDatabase);
                System.out.println("   User:     " + currentUser);
                System.out.println("   Version:  " + version.split("\n")[0]);
                System.out.println("=================================================");
            }
        }
    }
}
