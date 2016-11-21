package warehouse.model.webserver.db;

import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import warehouse.model.db.JDBCTemplate;
import warehouse.model.entities.Goods;
import warehouse.model.entities.Request;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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

    public static void insert(Goods goods) {
        jdbcTemplate.update("INSERT INTO Goods VALUES (?, ?, ?)", goods.getCode(), goods.getQuantity(), goods.getName());
    }

    public static Integer getGoodsCount(int good_id) {
        Integer quantity = -1;
        try {
            quantity = jdbcTemplate.queryForObject("SELECT quantity FROM GOODS WHERE id = ?", Integer.class, good_id);
        } catch (DataAccessException ignored) {}
        return quantity;
    }

    public static List<Goods> showAllGoods() {
        List<Goods> goods = new ArrayList<>();
        try {
            goods = jdbcTemplate.query("SELECT * FROM Goods",
                    (rs, rowNum) -> new Goods(rs.getInt("id"), rs.getInt("quantity"), rs.getString("name")));
        } catch (DataAccessException e) {
            return null;
        }
        return goods;
    }

    public static void addNewRequest(Request request) {
        int type_id = jdbcTemplate.queryForObject("SELECT id FROM OrderTypeList WHERE type = ?",
                Integer.class, request.getType().toString());
        Timestamp time = new Timestamp(System.currentTimeMillis());
        jdbcTemplate.update(
                "INSERT INTO Request (id, user_id, goods_id, quantity, type) VALUES (?, ?, ?, ?, ?)",
                    request.getId(), request.getUserId(), request.getUniqueCode(), request.getAmount(), type_id);
    }

    public static void payOrder(long id) {
        updateOrderType(id, "paid");
        int goodsId = jdbcTemplate.queryForObject("SELECT goods_id FROM Request WHERE id = ?",
                Integer.class, id);
        int availableCount = jdbcTemplate.queryForObject("SELECT quantity FROM Goods WHERE id = ?",
                Integer.class, goodsId);
        int bookedCount = jdbcTemplate.queryForObject("SELECT quantity FROM Request WHERE id = ?",
                Integer.class, id);
        if (bookedCount > availableCount) {
            // TODO: returns error (bad request) (i. e. it is still in progress)
        } else {
            availableCount -= bookedCount;
            jdbcTemplate.update("UPDATE Goods SET quantity = ? WHERE id = ?", availableCount, goodsId);
        }
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
