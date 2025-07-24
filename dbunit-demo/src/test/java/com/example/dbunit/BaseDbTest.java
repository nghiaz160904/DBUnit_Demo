// BaseDbTest.java
package com.example.dbunit;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseDbTest {
    protected Connection connection;
    protected IDatabaseTester databaseTester;

    @BeforeAll
    void setupDatabase() throws Exception {
        Properties props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("db.properties"));

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

        executeScript("sql/schema.sql");
        executeScript("sql/functions.sql");
        executeScript("sql/procedures.sql");
        executeScript("sql/triggers.sql");
    }

    @AfterAll
    void tearDownDatabase() throws Exception {
        if (databaseTester != null) {
            databaseTester.onTearDown();
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    protected void executeScript(String resourcePath) throws Exception {
        URL url = getClass().getClassLoader().getResource(resourcePath);
        if (url == null)
            throw new IllegalStateException("Resource not found: " + resourcePath);
        String sql = new String(Files.readAllBytes(Paths.get(url.toURI())));
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    protected void loadDataset(String datasetPath) throws Exception {
        IDataSet dataSet = new FlatXmlDataSetBuilder()
                .build(getClass().getResourceAsStream(datasetPath));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
    }
}