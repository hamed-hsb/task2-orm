package ir.negah.orm.core;

import ir.negah.orm.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleJdbcMapperTest {

    private Connection connection;
    private SimpleJdbcMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        mapper = new SimpleJdbcMapper(connection);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE users (
                    id INTEGER PRIMARY KEY,
                    first_name TEXT,
                    last_name TEXT,
                    user_name TEXT
                )
            """);

            stmt.execute("INSERT INTO users VALUES (101, 'Reza', 'Rad', 'rrad')");
            stmt.execute("INSERT INTO users VALUES (102, 'Sara', 'Karimi', 'skarimi')");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void findAll_whenUsersExist_shouldMapAllRecordsCorrectly() {
        List<User> users = mapper.findAll(User.class);

        assertNotNull(users);
        assertEquals(2, users.size());

        User user1 = users.getFirst();
        assertEquals(101L, user1.getId());
        assertEquals("Reza", user1.getFirstName());
        assertEquals("Rad", user1.getLastName());
        assertEquals("rrad", user1.getUserName());
    }

    @Test
    void findAll_whenTableIsEmpty_shouldReturnEmptyList() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM users");
        }

        List<User> users = mapper.findAll(User.class);

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }
}
