package warehouse.model.webserver.db;

import org.springframework.jdbc.core.JdbcTemplate;

public class SQLExecutor {
    private static JdbcTemplate jdbcTemplate = JDBCTemplate.getInstance();

    public static void insert(String login, String password) {
        jdbcTemplate.update(
                "INSERT INTO User (login, password) VALUES (?,?)", login, password);
    }
}
