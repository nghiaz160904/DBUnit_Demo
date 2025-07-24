// FunctionTests.java
package com.example.dbunit;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class FunctionTests extends BaseDbTest {

    @Test
    @Order(1)
    void testGetUserPostCountFunction() throws Exception {
        loadDataset("/datasets/function-dataset.xml");
        try (PreparedStatement ps = connection.prepareStatement("SELECT get_user_post_count(?)")) {
            ps.setInt(1, 1);
            ResultSet rs = ps.executeQuery();
            rs.next();
            assertEquals(2, rs.getInt(1));
        }
    }

    @Test
    @Order(2)
    void testFunctionReturnsZeroForUserWithNoPosts() throws Exception {
        loadDataset("/datasets/schema-dataset.xml");
        try (PreparedStatement ps = connection.prepareStatement("SELECT get_user_post_count(?)")) {
            ps.setInt(1, 2);
            ResultSet rs = ps.executeQuery();
            rs.next();
            assertEquals(0, rs.getInt(1));
        }
    }
}