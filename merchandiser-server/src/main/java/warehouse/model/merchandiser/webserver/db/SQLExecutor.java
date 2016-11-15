package warehouse.model.merchandiser.webserver.db;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import warehouse.model.db.JDBCTemplate;
import warehouse.model.entities.Request;
import warehouse.model.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.ResourceBundle;

public class SQLExecutor {
    private static JdbcTemplate jdbcTemplate;
    static {
        ResourceBundle bundle = ResourceBundle.getBundle("mh", Locale.US);
        jdbcTemplate = JDBCTemplate.getInstance(JDBCTemplate.Type.MH, bundle.getString("db.location"),
                bundle.getString("db.login"), bundle.getString("db.password"), new String[]{"db/create-db.sql"});
        String sqlEmptyQuery = "SELECT COUNT(*) FROM ";
        ResultSetExtractor<Boolean> emptyChecker = rs -> {
            rs.next();
            int count = rs.getInt(1);
            return count == 0;
        };
        boolean isTypeListEmpty = jdbcTemplate.query(sqlEmptyQuery + "OrderTypeList", emptyChecker);
        boolean isStatusListEmpty = jdbcTemplate.query(sqlEmptyQuery + "StatusList", emptyChecker);
        boolean isUserTableEmpty = jdbcTemplate.query(sqlEmptyQuery + "User", emptyChecker);
        if (isTypeListEmpty || isStatusListEmpty || isUserTableEmpty) {
            Connection dbConnection = null;
            try {
                dbConnection = jdbcTemplate.getDataSource().getConnection();
                if (isTypeListEmpty) {
                    ScriptUtils.executeSqlScript(dbConnection, new ClassPathResource("db/OrderTypeList-first-fill.sql"));
                }
                if (isStatusListEmpty) {
                    ScriptUtils.executeSqlScript(dbConnection, new ClassPathResource("db/StatusList-first-fill.sql"));
                }
                if (isUserTableEmpty) {
                    ScriptUtils.executeSqlScript(dbConnection, new ClassPathResource("db/User-first-fill.sql"));
                }
            } catch (SQLException ignored) {
            } finally {
                try {
                    if (dbConnection != null) {
                        dbConnection.close();
                    }
                } catch (SQLException ignored) {}
            }
        }
    }

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
        int status_id = jdbcTemplate.queryForObject("SELECT id FROM StatusList WHERE status = ?",
                 Integer.class, "in progress");
        Timestamp time = new Timestamp(System.currentTimeMillis());
        jdbcTemplate.update(
                "INSERT INTO Request (id, user_id, goods_id, quantity, type, date, attempts_count, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    request.getId(), request.getUserId(), request.getUniqueCode(), request.getAmount(), type_id, time, 1, status_id);
    }

    public static void payOrder(long id) {
        updateOrderType(id, "paid", "in progress");
    }

    public static void cancelOrder(long id) {
        updateOrderType(id, "canceled", "canceled");
    }

    private static void updateOrderType(long id, String type, String status) {
        int type_id = jdbcTemplate.queryForObject("SELECT id FROM OrderTypeList WHERE type = ?",
                Integer.class, type);
        int status_id = jdbcTemplate.queryForObject("SELECT id FROM StatusList WHERE status = ?",
                Integer.class, status);
        Timestamp time = new Timestamp(System.currentTimeMillis());
        jdbcTemplate.update("UPDATE Request SET type = ? WHERE id = ?", type_id, id);
        jdbcTemplate.update("UPDATE Request SET status = ? WHERE id = ?", status_id, id);
        jdbcTemplate.update("UPDATE Request SET attempts_count = ? WHERE id = ?", 1, id);
        jdbcTemplate.update("UPDATE Request SET date = ? WHERE id = ?", time, id);
    }
}
