package warehouse.model.webserver.db;

import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import warehouse.model.db.JDBCTemplate;
import warehouse.model.entities.Goods;
import warehouse.model.entities.Request;
import warehouse.model.loggers.Loggers;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SQLExecutor {
    private static JdbcTemplate jdbcTemplate;
    static {
        ResourceBundle bundle = ResourceBundle.getBundle("wh", Locale.US);
        jdbcTemplate = JDBCTemplate.getInstance(JDBCTemplate.Type.WH, bundle.getString("db.location"),
                bundle.getString("db.login"), bundle.getString("db.password"), new String[]{"db/create-db.sql"});
        String sqlEmptyQuery = "SELECT COUNT(*) FROM ";
        ResultSetExtractor<Boolean> emptyChecker = rs -> {
            rs.next();
            int count = rs.getInt(1);
            return count == 0;
        };
        boolean isTypeListEmpty = jdbcTemplate.query(sqlEmptyQuery + "OrderTypeList", emptyChecker);
        boolean isGoodsTableEmpty = jdbcTemplate.query(sqlEmptyQuery + "Goods", emptyChecker);
        if (isTypeListEmpty || isGoodsTableEmpty) {
            Connection dbConnection = null;
            try {
                dbConnection = jdbcTemplate.getDataSource().getConnection();
                if (isTypeListEmpty) {
                    ScriptUtils.executeSqlScript(dbConnection, new ClassPathResource("db/OrderTypeList-first-fill.sql"));
                }
                if (isGoodsTableEmpty) {
                    ScriptUtils.executeSqlScript(dbConnection, new ClassPathResource("db/Goods-first-fill.sql"));
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
    private static final Logger log = Loggers.getULogger(SQLExecutor.class, "wh");

    public static HttpStatus insert(Goods goods) {
        try {
            jdbcTemplate.update("INSERT INTO Goods VALUES (?, ?, ?)", goods.getCode(), goods.getQuantity(), goods.getName());
            return HttpStatus.OK;
        } catch (DataAccessException e) {
            log.error("Cannot insert new item: " + e.getMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public static HttpStatus insertSeveralGoods(List<Goods> goods) {
        StringJoiner sql = new StringJoiner(", ", "INSERT INTO Goods VALUES ", ";");
        for (Goods g : goods) {
            sql.add(g.toQuery());
        }
        try {
            jdbcTemplate.update(sql.toString());
            return HttpStatus.OK;
        } catch (DataAccessException e) {
            log.error("Cannot insert new goods: " + e.getMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public static HttpStatus updateProductCount(int id, int add) {
        try {
            int currentCount = jdbcTemplate.queryForObject("SELECT quantity FROM Goods WHERE id = ?",
                    Integer.class, id);
            currentCount += add;
            jdbcTemplate.update("UPDATE Goods SET quantity = ? WHERE id = ?", currentCount, id);
            return HttpStatus.OK;
        } catch (DataAccessException e) {
            log.error("Cannot update product count: " + e.getMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public static Integer getGoodsCount(int good_id) {
        Integer quantity = -1;
        try {
            quantity = jdbcTemplate.queryForObject("SELECT quantity FROM GOODS WHERE id = ?", Integer.class, good_id);
        } catch (DataAccessException e) {
            log.error("Cannot retrieve goods count from db: " + e.getMessage());
        }
        return quantity;
    }

    public static List<Goods> showAllGoods() {
        List<Goods> goods = new ArrayList<>();
        try {
            goods = jdbcTemplate.query("SELECT * FROM Goods",
                    (rs, rowNum) -> new Goods(rs.getInt("id"), rs.getInt("quantity"), rs.getString("name")));
        } catch (DataAccessException e) {
            log.error("Cannot retrieve goods from db: " + e.getMessage());
            return null;
        }
        return goods;
    }

    public static HttpStatus addNewRequest(Request request) {
        try {
            int exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Request WHERE id = ?", Integer.class, request.getId());
            if (exists != 0) {
                return HttpStatus.OK;
            }
            int typeId = getTypeId(request.getType().toString());
            jdbcTemplate.update(
                    "INSERT INTO Request (id, user_id, goods_id, quantity, type) VALUES (?, ?, ?, ?, ?)",
                    request.getId(), request.getUserId(), request.getUniqueCode(), request.getAmount(), typeId);
            return HttpStatus.OK;
        } catch (DataAccessException e) {
            log.error("Cannot add new request to db: " + e.getMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public static HttpStatus payOrder(long id) {
        try {
            int exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Request WHERE id = ? AND type = ?", Integer.class,
                    id, getTypeId("paid"));
            if (exists != 0) {
                return HttpStatus.OK;
            }
            int goodsId = jdbcTemplate.queryForObject("SELECT goods_id FROM Request WHERE id = ?",
                    Integer.class, id);
            int availableCount = jdbcTemplate.queryForObject("SELECT quantity FROM Goods WHERE id = ?",
                    Integer.class, goodsId);
            int bookedCount = jdbcTemplate.queryForObject("SELECT quantity FROM Request WHERE id = ?",
                    Integer.class, id);
            if (bookedCount > availableCount) {
                log.error("[FATAL] Available count of goods less than booked count. Urgently add necessary goods.");
                return HttpStatus.NOT_ACCEPTABLE;
            } else {
                availableCount -= bookedCount;
                jdbcTemplate.update("UPDATE Goods SET quantity = ? WHERE id = ?", availableCount, goodsId);
                updateOrderType(id, "paid");
                return HttpStatus.OK;
            }
        } catch (DataAccessException e) {
            log.error("Cannot pay order: " + e.getMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public static HttpStatus cancelOrder(long id) {
        try {
            updateOrderType(id, "canceled");
            return HttpStatus.OK;
        } catch (DataAccessException e) {
            log.error("Cannot mark order as canceled: " + e.getMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private static void updateOrderType(long id, String type) {
        int typeId = getTypeId(type);
        jdbcTemplate.update("UPDATE Request SET type = ? WHERE id = ?", typeId, id);
    }

    public static void deleteOldRequests() {
        try {
            jdbcTemplate.query("SELECT * FROM Request", rs -> {
                int type = rs.getInt("type");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                String curDate = dateFormat.format(new Date());
                long requestId = rs.getLong("id");
                String prevDate = (String.valueOf(requestId)).substring(0, 8);
                int cur = Integer.parseInt(curDate);
                int prev = Integer.parseInt(prevDate);
                int dif = cur - prev;
                if (type == getTypeId("canceled") || (type == getTypeId("booked") && dif >= 300)) {
                    jdbcTemplate.update("DELETE FROM Request WHERE id = ?", requestId);
                }
            });
        } catch (DataAccessException e) {
            log.error("Cannot delete old requests from db: " + e.getMessage());
        }
    }

    private static int getTypeId(String type) {
        return jdbcTemplate.queryForObject("SELECT id FROM OrderTypeList WHERE type = ?",
                Integer.class, type);
    }
}
