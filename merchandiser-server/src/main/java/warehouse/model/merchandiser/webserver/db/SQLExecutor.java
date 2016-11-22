package warehouse.model.merchandiser.webserver.db;

import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import warehouse.model.db.JDBCTemplate;
import warehouse.model.entities.Request;
import warehouse.model.entities.User;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class SQLExecutor {
    private static JdbcTemplate jdbcTemplate;
    private static int MAX_ATTEMPT_COUNT = 10;
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
            try {
                if (preparedStatement.executeUpdate() == 1) {
                    rs = preparedStatement.getGeneratedKeys();
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                return -2;
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

    public static Integer checkUser(User user) {
        List<Integer> id;
        try {
            id = jdbcTemplate.query("SELECT id FROM User WHERE login = ? and password = ?",
                    new String[]{user.getLogin(), user.getPassword()}, (rs, rowNum) -> rs.getInt("id"));
        } catch (DataAccessException e) {
            return -1;
        }
        if (id == null || id.size() == 0) {
            return -2;
        }
        return id.get(0);
    }

    public static void addNewRequest(Request request) {
        int type_id = getTypeId(request.getType().toString());
        int status_id = getStatusId(request.getStatus().toString());
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
        int type_id = getTypeId(type);
        int status_id = getStatusId(status);
        jdbcTemplate.update("UPDATE Request SET type = ? WHERE id = ?", type_id, id);
        jdbcTemplate.update("UPDATE Request SET status = ? WHERE id = ?", status_id, id);
        updateAttemptsCount(id, 0);
    }

    public static void updateOrderStatus(long id, String status) {
        int status_id = getStatusId(status);
        jdbcTemplate.update("UPDATE Request SET status = ? WHERE id = ?", status_id, id);
        updateAttemptsCount(id, 0);
    }

    private static void updateAttemptsCount(long id, int attempts_count) {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        jdbcTemplate.update("UPDATE Request SET attempts_count = ? WHERE id = ?", attempts_count, id);
        jdbcTemplate.update("UPDATE Request SET date = ? WHERE id = ?", time, id);
    }

    public static List<Request> allUserOrders(int id) {
        List<Request> requests = new ArrayList<>();
        try {
            requests = jdbcTemplate.query("SELECT * FROM Request WHERE user_id = ?", new Object[]{id}, (rs, rowNum) -> {
                int type = rs.getInt("type");
                String typeName = getTypeName(type);
                int status = rs.getInt("status");
                String statusName = getStatusName(status);
                return new Request(rs.getLong("id"), rs.getInt("user_id"), rs.getInt("goods_id"), rs.getInt("quantity"),
                        Request.RequestType.getRequestTypeFromString(typeName), Request.RequestStatus.getRequestStatusFromString(statusName));
            });
        } catch (DataAccessException e) {
            return null;
        }
        return requests;
    }

    public static List<Request> getAllInProgressRequests() {
        List<Request> requests = new ArrayList<>();
        int status = getStatusId("in progress");
        try {
            requests = jdbcTemplate.query("SELECT * FROM Request WHERE status = ?", new Object[]{status}, (rs, rowNum) -> {
                int attempts_count = rs.getInt("attempts_count");
                if (attempts_count >= MAX_ATTEMPT_COUNT) {
                    return null;
                }
                int type = rs.getInt("type");
                String typeName = getTypeName(type);
                return new Request(rs.getLong("id"), rs.getInt("user_id"), rs.getInt("goods_id"), rs.getInt("quantity"),
                        Request.RequestType.getRequestTypeFromString(typeName), Request.RequestStatus.IN_PROGRESS);
            });
        } catch (DataAccessException e) {
            return null;
        }
        return requests;
    }

    public static void incrementAttemptsCount(long id) {
        try {
            int current_count = jdbcTemplate.queryForObject("SELECT attempts_count FROM Request WHERE id = ?",
                    Integer.class, id);
            current_count++;
            updateAttemptsCount(id, current_count);
        } catch (DataAccessException ignored) {}
    }

    public static void deleteOldRequests() {
        try {
            jdbcTemplate.query("SELECT * FROM Request", rs -> {
                int booked = getTypeId("booked");
                int canceled = getTypeId("canceled");
                int type = rs.getInt("type");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                String curDate = dateFormat.format(new Date());
                long requestId = rs.getLong("id");
                String id = String.valueOf(requestId);
                id = id.substring(0, 8);
                int cur = Integer.parseInt(curDate);
                int prev = Integer.parseInt(id);
                int dif = cur - prev;
                if (type == canceled || (type == booked && dif >= 300)) {
                    jdbcTemplate.update("DELETE FROM Request WHERE id = ?", requestId);
                }
            });
        } catch (DataAccessException ignored) {}
    }

    public static void resetAttemptsCount() {
        try {
            jdbcTemplate.query("SELECT * FROM Request WHERE attempts_count >= ?", new Object[]{MAX_ATTEMPT_COUNT}, rs -> {
                long id = rs.getLong("id");
                jdbcTemplate.update("UPDATE Request SET attempts_count = 0 WHERE id = ?", id);
            });
        } catch (DataAccessException ignored) {}
    }

    private static int getStatusId(String status) {
        return jdbcTemplate.queryForObject("SELECT id FROM StatusList WHERE status = ?",
                Integer.class, status);
    }

    private static int getTypeId(String type) {
        return jdbcTemplate.queryForObject("SELECT id FROM OrderTypeList WHERE type = ?",
                Integer.class, type);
    }

    private static String getStatusName(int id) {
        return jdbcTemplate.queryForObject("SELECT status FROM StatusList WHERE id = ?",
                String.class, id);
    }

    private static String getTypeName(int id) {
        return jdbcTemplate.queryForObject("SELECT type FROM OrderTypeList WHERE id = ?",
                String.class, id);
    }
}
