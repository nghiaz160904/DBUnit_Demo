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
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FunctionTest extends AbstractDbTest {
    private IDatabaseTester databaseTester;
    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        var props = new Properties();
        System.out.println(getClass().getClassLoader().getResource("db.properties"));
        System.out.println(getClass().getResource("/datasets/function-dataset.xml"));

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
                .build(getClass().getResourceAsStream("/datasets/function-dataset.xml"));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
    }

    @Test
    void testGetUserPostCount() throws Exception {
        var stmt = connection.prepareStatement("SELECT get_user_post_count(?)");
        stmt.setInt(1, 1);
        var rs = stmt.executeQuery();
        rs.next();
        assertEquals(2, rs.getInt(1));
    }
}
