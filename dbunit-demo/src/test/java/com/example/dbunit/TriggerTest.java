package com.example.dbunit;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TriggerTest extends AbstractDbTest {
    private IDatabaseTester databaseTester;
    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        var props = new Properties();
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

        IDataSet dataSet = new FlatXmlDataSetBuilder()
                .build(getClass().getResourceAsStream("/datasets/trigger-dataset.xml"));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
    }

    @Test
    void testTriggerSetsCreatedAt() throws Exception {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO posts(user_id, title, content) VALUES (?, ?, ?)");
        ps.setInt(1, 1);
        ps.setString(2, "Triggered Post");
        ps.setString(3, "Content");
        ps.executeUpdate();

        ResultSet rs = connection.createStatement()
                .executeQuery("SELECT created_at FROM posts WHERE title = 'Triggered Post'");
        rs.next();
        assertNotNull(rs.getTimestamp(1), "created_at must be set by trigger");
    }
}
