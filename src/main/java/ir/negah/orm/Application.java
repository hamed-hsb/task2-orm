package ir.negah.orm;

import ir.negah.orm.core.SimpleJdbcMapper;
import ir.negah.orm.entity.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

public class Application {

    private static final String DB_URL = "jdbc:sqlite::memory:";

    public static void main(String[] args) throws Exception {
        try (Connection connection = DriverManager.getConnection(DB_URL)) {
            initializeDatabase(connection);

            SimpleJdbcMapper mapper = new SimpleJdbcMapper(connection);
            List<User> users = mapper.findAll(User.class);

            users.forEach(System.out::println);
        }
    }

    private static void initializeDatabase(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE users (
                    id INTEGER PRIMARY KEY,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    user_name TEXT NOT NULL
                )
            """);

            statement.execute("INSERT INTO users (id, first_name, last_name, user_name) VALUES (1, 'Farhad', 'Nazari', 'fnazari')");
            statement.execute("INSERT INTO users (id, first_name, last_name, user_name) VALUES (2, 'Mina', 'Javadi', 'mjavadi')");
            statement.execute("INSERT INTO users (id, first_name, last_name, user_name) VALUES (3, 'Ali', 'Mohammadi', 'amohammadi')");
        }
    }
}
