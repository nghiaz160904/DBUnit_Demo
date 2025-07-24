// ProcedureTests.java
package com.example.dbunit;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class ProcedureTests extends BaseDbTest {

    @Test
    @Order(1)
    void testSpCreatePostProcedureSetsCreatedAt() throws Exception {
        loadDataset("/datasets/procedure-dataset.xml");
        try (Statement st = connection.createStatement()) {
            st.execute("CALL sp_create_post(2, 'New Title', 'New Content');");
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*), MIN(created_at) IS NOT NULL FROM posts WHERE user_id = 2")) {
            ResultSet rs = ps.executeQuery();
            rs.next();
            assertEquals(1, rs.getInt(1));
            assertTrue(rs.getBoolean(2));
        }
    }
}