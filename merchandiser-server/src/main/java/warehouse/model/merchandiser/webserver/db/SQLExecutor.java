package warehouse.model.merchandiser.webserver.db;

import org.springframework.jdbc.core.JdbcTemplate;
import warehouse.model.entities.GetRequest;
import warehouse.model.entities.OrderRequest;
import warehouse.model.entities.User;

import java.sql.*;

public class SQLExecutor {
    private static JdbcTemplate jdbcTemplate = JDBCTemplate.getInstance();

    public static Integer insert(User user) {
        String sql = "INSERT INTO User (login, password) VALUES (?,?)";
        PreparedStatement preparedStatement = null;
        Connection dbConnection = null;
        ResultSet rs = null;
        try {
            dbConnection = jdbcTemplate.getDataSource().getConnection();
            preparedStatement = dbConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getLogin());
            preparedStatement.setString(2, user.getPassword());
            if (preparedStatement.executeUpdate() == 1) {
                rs = preparedStatement.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            return -1;
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (dbConnection != null) {
                        dbConnection.close();
                }
            } catch (SQLException ignored) {}
        }
        return -1;
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
