// TriggerTests.java
package com.example.dbunit;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class TriggerTests extends BaseDbTest {

        @Test
        @Order(1)
        void testTriggerSetsCreatedAtOnInsert() throws Exception {
                loadDataset("/datasets/trigger-dataset.xml");
                try (PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO posts(user_id, title, content) VALUES (?, ?, ?);")) {
                        ps.setInt(1, 1);
                        ps.setString(2, "Triggered Post");
                        ps.setString(3, "Content");
                        ps.executeUpdate();
                }
                try (

                                ResultSet rs = connection.createStatement()
                                                .executeQuery("SELECT created_at FROM posts WHERE title='Triggered Post';")) {
                        rs.next();
                        assertNotNull(rs.getTimestamp(1));
                }
        }
}
