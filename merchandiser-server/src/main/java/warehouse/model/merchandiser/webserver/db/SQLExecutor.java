package warehouse.model.merchandiser.webserver.db;

import org.springframework.jdbc.core.JdbcTemplate;
import warehouse.model.db.JDBCTemplate;
import warehouse.model.entities.Request;
import warehouse.model.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.ResourceBundle;

public class SQLExecutor {
    private static ResourceBundle bundle = ResourceBundle.getBundle("mh", Locale.US);
    private static JdbcTemplate jdbcTemplate = JDBCTemplate.getInstance(JDBCTemplate.Type.MH, bundle.getString("db.location"),
            bundle.getString("db.login"), bundle.getString("db.password"), new String[]{"db/create-db.sql"});

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

    public static void addNewRequest(Request request) {
        int type_id = jdbcTemplate.queryForObject("SELECT id FROM OrderTypeList WHERE type = ?",
                Integer.class, request.getType().toString());
        jdbcTemplate.update(
                "INSERT INTO Request (id, user_id, goods_id, quantity, type) VALUES (?, ?, ?, ?, ?)", request.getId(),
                    request.getUserId(), request.getUniqueCode(), request.getAmount(), type_id);
    }

    public static void payOrder(long id) {
        updateOrderType(id, "paid");
    }

    public static void cancelOrder(long id) {
        updateOrderType(id, "canceled");
    }

    private static void updateOrderType(long id, String type) {
        int type_id = jdbcTemplate.queryForObject("SELECT id FROM OrderTypeList WHERE type = ?",
                Integer.class, type);
        jdbcTemplate.update("UPDATE Request SET type = ? WHERE id = ?", type_id, id);
    }
}
