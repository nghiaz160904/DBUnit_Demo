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
import java.sql.CallableStatement;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProcedureTest extends AbstractDbTest {
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
                .build(getClass().getResourceAsStream("/datasets/procedure-dataset.xml"));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
    }

    @Test
    void testSpCreatePost() throws Exception {
        try (CallableStatement cs = connection.prepareCall("CALL sp_create_post(?,?,?)")) {
            cs.setInt(1, 2);
            cs.setString(2, "New Title");
            cs.setString(3, "New Content");
            cs.execute();
        }

        var rs = connection.createStatement()
                .executeQuery("SELECT COUNT(*) FROM posts WHERE user_id = 2");
        rs.next();
        assertEquals(1, rs.getInt(1));
    }
}
