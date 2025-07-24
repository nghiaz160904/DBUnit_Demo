// SchemaTests.java
package com.example.dbunit;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class SchemaTests extends BaseDbTest {

    @Test
    @Order(1)
    void testSchemaRowCounts() throws Exception {
        loadDataset("/datasets/schema-dataset.xml");
        try (Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            assertEquals(2, rs.getInt(1));
            rs = st.executeQuery("SELECT COUNT(*) FROM posts");
            rs.next();
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    @Order(2)
    void testSchemaForeignKeyConstraintViolation() throws Exception {
        loadDataset("/datasets/trigger-dataset.xml");
        SQLException ex = assertThrows(SQLException.class, () -> {
            try (Statement st = connection.createStatement()) {
                st.execute("INSERT INTO posts(user_id, title, content) VALUES (999, 'Invalid FK', 'Fail');");
            }
        });
        assertTrue(ex.getMessage().toLowerCase().contains("foreign key"));
    }
}