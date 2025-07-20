// src/test/java/com/example/dbunit/AbstractDbTest.java
package com.example.dbunit;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.junit.jupiter.api.BeforeAll;

import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public abstract class AbstractDbTest {
    protected static Connection connection;
    protected static IDatabaseTester databaseTester;

    @BeforeAll
    static void globalSetUp() throws Exception {
        // Load DB props
        Properties props = new Properties();
        props.load(AbstractDbTest.class.getClassLoader()
                .getResourceAsStream("db.properties"));

        Class.forName(props.getProperty("jdbc.driverClassName"));
        connection = DriverManager.getConnection(
                props.getProperty("jdbc.url"),
                props.getProperty("jdbc.username"),
                props.getProperty("jdbc.password"));
        databaseTester = new JdbcDatabaseTester(
                props.getProperty("jdbc.driverClassName"),
                props.getProperty("jdbc.url"),
                props.getProperty("jdbc.username"),
                props.getProperty("jdbc.password"));

        // Chạy nguyên script SQL (schema, function, proc, trigger)
        executeScript("sql/schema.sql");
        executeScript("sql/functions.sql");
        executeScript("sql/procedures.sql");
        executeScript("sql/triggers.sql");
    }

    private static void executeScript(String resourcePath) throws Exception {
        URL url = AbstractDbTest.class.getClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new IllegalStateException("Resource not found: " + resourcePath);
        }
        String sql = new String(Files.readAllBytes(Paths.get(url.toURI())));
        try (Statement st = connection.createStatement()) {
            // cho JDBC chạy nguyên khối; PostgreSQL JDBC sẽ tự xử lý nhiều câu lệnh
            st.execute(sql);
        }
    }
}
