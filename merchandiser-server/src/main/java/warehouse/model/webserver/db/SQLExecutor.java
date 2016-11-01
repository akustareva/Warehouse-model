package warehouse.model.webserver.db;

import org.springframework.jdbc.core.JdbcTemplate;
import warehouse.model.entities.GetRequest;
import warehouse.model.entities.OrderRequest;
import warehouse.model.entities.User;

public class SQLExecutor {
    private static JdbcTemplate jdbcTemplate = JDBCTemplate.getInstance();

    public static void insert(User user) {
        jdbcTemplate.update(
                "INSERT INTO User (login, password) VALUES (?,?)", user.getLogin(), user.getPassword());
    }

    public static void addNewRequest(OrderRequest request) {
        jdbcTemplate.update(
                "INSERT INTO Request (user_id, unique_code, amount, type) VALUES (?,?,?,?)", request.getUserId(),
                 request.getUniqueCode(), request.getAmount(), request.getType());
    }

    public static void addNewRequest(GetRequest request) {
        jdbcTemplate.update(
                "INSERT INTO Request (user_id, unique_code, type) VALUES (?,?,?)", request.getUserId(),
                request.getUniqueCode(), "get");
    }
}
