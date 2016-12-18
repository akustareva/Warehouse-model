package warehouse.model.merchandiser.webserver.db;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import warehouse.model.db.JDBCTemplate;
import warehouse.model.entities.Goods;
import warehouse.model.entities.Order;
import warehouse.model.entities.Request;
import warehouse.model.entities.User;
import warehouse.model.loggers.Loggers;

import java.io.IOException;
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
    private static final Logger log = Loggers.getULogger(SQLExecutor.class, "mh");

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

    public static HttpStatus addNewRequest(Request request) {
        try {
            int type_id = getTypeId(request.getType().toString());
            int status_id = getStatusId(request.getStatus().toString());
            Timestamp time = new Timestamp(System.currentTimeMillis());
            jdbcTemplate.update(
                    "INSERT INTO Request (id, user_id, goods_id, quantity, type, date, attempts_count, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    request.getId(), request.getUserId(), request.getUniqueCode(), request.getAmount(), type_id, time, 1, status_id);
            return HttpStatus.OK;
        } catch (DataAccessException e) {
            log.error("Cannot add new request: " + e.getMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public static void payOrder(long id) {
        updateOrderType(id, "paid", "in progress");
    }

    public static void cancelOrder(long id) {
        updateOrderType(id, "canceled", "canceled");
    }

    private static void updateOrderType(long id, String type, String status) {
        try {
            int type_id = getTypeId(type);
            int status_id = getStatusId(status);
            jdbcTemplate.update("UPDATE Request SET type = ? WHERE id = ?", type_id, id);
            jdbcTemplate.update("UPDATE Request SET status = ? WHERE id = ?", status_id, id);
            updateAttemptsCount(id, 0);
        } catch (DataAccessException e) {
            log.error("Cannot change order type on " + type + ": " + e.getMessage());
        }
    }

    public static void updateOrderStatus(long id, String status) {
        try {
            int status_id = getStatusId(status);
            jdbcTemplate.update("UPDATE Request SET status = ? WHERE id = ?", status_id, id);
            updateAttemptsCount(id, 0);
        } catch (DataAccessException e) {
            log.error("Cannot change order status on " + status + ": " + e.getMessage());
        }
    }

    private static void updateAttemptsCount(long id, int attempts_count) {
        try {
            Timestamp time = new Timestamp(System.currentTimeMillis());
            jdbcTemplate.update("UPDATE Request SET attempts_count = ? WHERE id = ?", attempts_count, id);
            jdbcTemplate.update("UPDATE Request SET date = ? WHERE id = ?", time, id);
        } catch (DataAccessException e) {
            log.error("Cannot update attempts count: " + e.getMessage());
        }
    }

    public static List<Order> allUserOrders(int id) {
        List<Order> requests = new ArrayList<>();
        try {
            requests = jdbcTemplate.query("SELECT * FROM Request WHERE user_id = ?", new Object[]{id}, (rs, rowNum) -> {
                int type = rs.getInt("type");
                String typeName = getTypeName(type);
                int status = rs.getInt("status");
                String statusName = getStatusName(status);
                String goodsName = jdbcTemplate.queryForObject("SELECT name FROM Goods WHERE id = ?", String.class, rs.getInt("goods_id"));
                return new Order(rs.getLong("id"), goodsName, rs.getInt("quantity"), Request.RequestType.getRequestTypeFromString(typeName),
                        Request.RequestStatus.getRequestStatusFromString(statusName));
            });
        } catch (DataAccessException e) {
            log.error("Cannot show user orders: " + e.getMessage());
            return null;
        }
        return requests;
    }

    public static List<Request> getAllInProgressRequests() {
        List<Request> requests = new ArrayList<>();
        try {
            int status = getStatusId("in progress");
            requests = jdbcTemplate.query("SELECT * FROM Request WHERE status = ?", new Object[]{status}, (rs, rowNum) -> {
                int attempts_count = rs.getInt("attempts_count");
                if (attempts_count >= MAX_ATTEMPT_COUNT) {
                    log.error("[FATAL] Attempts count of request number " + rs.getLong("id") + " more than maximum allowable.");
                    return null;
                }
                int type = rs.getInt("type");
                String typeName = getTypeName(type);
                return new Request(rs.getLong("id"), rs.getInt("user_id"), rs.getInt("goods_id"), rs.getInt("quantity"),
                        Request.RequestType.getRequestTypeFromString(typeName), Request.RequestStatus.IN_PROGRESS);
            });
        } catch (DataAccessException e) {
            log.error("Cannot retrieve in progress requests from db: " + e.getMessage());
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
        } catch (DataAccessException e) {
            log.error("Cannot increment attempts count of request number " + id + ": " + e.getMessage());
        }
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
        } catch (DataAccessException e) {
            log.error("Cannot delete old requests: " + e.getMessage());
        }
    }

    public static HttpStatus resetAttemptsCount() {
        try {
            jdbcTemplate.query("SELECT * FROM Request WHERE attempts_count >= ?", new Object[]{MAX_ATTEMPT_COUNT}, rs -> {
                long id = rs.getLong("id");
                jdbcTemplate.update("UPDATE Request SET attempts_count = 0 WHERE id = ?", id);
            });
            return HttpStatus.OK;
        } catch (DataAccessException e) {
            log.error("Cannot reset attempts count: " + e.getMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public static void updateGoodsTable(JsonNode goodsJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Goods> goods = mapper.readValue(mapper.treeAsTokens(goodsJson), new TypeReference<List<Goods>>() {});
            StringJoiner sql = new StringJoiner("), (", "MERGE INTO Goods VALUES (", ");");
            for (Goods g : goods) {
                sql.add(g.getCode() + ", '" + g.getName() + "'");
            }
            jdbcTemplate.update(sql.toString());
        } catch (DataAccessException | IOException e) {
            log.error("Cannot update goods table: " + e.getMessage());
        }
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
