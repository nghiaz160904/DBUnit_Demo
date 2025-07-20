// src/test/java/com/example/dbunit/DbUnitAllTests.java
package com.example.dbunit;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.*;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DbUnitAllTests {
    private Connection connection;
    private IDatabaseTester databaseTester;

    @BeforeAll
    void setupDatabase() throws Exception {
        // Load DB config
        Properties props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("db.properties"));

        // Connect
        Class.forName(props.getProperty("jdbc.driverClassName"));
        connection = DriverManager.getConnection(
            props.getProperty("jdbc.url"),
            props.getProperty("jdbc.username"),
            props.getProperty("jdbc.password")
        );
        databaseTester = new JdbcDatabaseTester(
            props.getProperty("jdbc.driverClassName"),
            props.getProperty("jdbc.url"),
            props.getProperty("jdbc.username"),
            props.getProperty("jdbc.password")
        );

        // Khởi tạo schema, function, procedure, trigger
        executeScript("sql/schema.sql");
        executeScript("sql/functions.sql");
        executeScript("sql/procedures.sql");
        executeScript("sql/triggers.sql");
    }

    private void executeScript(String resourcePath) throws Exception {
        URL url = getClass().getClassLoader().getResource(resourcePath);
        if (url == null) throw new IllegalStateException("Resource not found: " + resourcePath);
        String sql = new String(Files.readAllBytes(Paths.get(url.toURI())));
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
        }
    }

    private void loadDataset(String datasetPath) throws Exception {
        IDataSet dataSet = new FlatXmlDataSetBuilder()
            .build(getClass().getResourceAsStream(datasetPath));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
    }

    @Test
    void testSchemaRowCounts() throws Exception {
        loadDataset("/datasets/schema-dataset.xml");

        try (Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            assertEquals(2, rs.getInt(1), "Users count should be 2");
            rs = st.executeQuery("SELECT COUNT(*) FROM posts");
            rs.next();
            assertEquals(1, rs.getInt(1), "Posts count should be 1");
        }
    }

    @Test
    void testGetUserPostCountFunction() throws Exception {
        loadDataset("/datasets/function-dataset.xml");

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT get_user_post_count(?)")) {
            ps.setInt(1, 1);
            ResultSet rs = ps.executeQuery();
            rs.next();
            assertEquals(2, rs.getInt(1), "Function should return 2 for user 1");
        }
    }

    @Test
    void testSpCreatePostProcedure() throws Exception {
        loadDataset("/datasets/procedure-dataset.xml");

        try (Statement st = connection.createStatement()) {
            st.execute("CALL sp_create_post(2, 'New Title', 'New Content');");
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM posts WHERE user_id = 2")) {
            ResultSet rs = ps.executeQuery();
            rs.next();
            assertEquals(1, rs.getInt(1), "Procedure should insert one post for user 2");
        }
    }

    @Test
    void testTriggerSetsCreatedAt() throws Exception {
        loadDataset("/datasets/trigger-dataset.xml");

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO posts(user_id, title, content) VALUES (?, ?, ?)")) {
            ps.setInt(1, 1);
            ps.setString(2, "Triggered Post");
            ps.setString(3, "Content");
            ps.executeUpdate();
        }

        try (ResultSet rs = connection.createStatement()
                .executeQuery("SELECT created_at FROM posts WHERE title = 'Triggered Post'")) {
            rs.next();
            assertNotNull(rs.getTimestamp(1), "Trigger must set created_at");
        }
    }
}
