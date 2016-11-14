package warehouse.model.webserver.db;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import warehouse.model.db.JDBCTemplate;
import warehouse.model.entities.Goods;
import warehouse.model.entities.Request;

import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class SQLExecutor {
    private static ResourceBundle bundle = ResourceBundle.getBundle("wh", Locale.US);
    private static JdbcTemplate jdbcTemplate = JDBCTemplate.getInstance(JDBCTemplate.Type.WH, bundle.getString("db.location"),
            bundle.getString("db.login"), bundle.getString("db.password"), new String[]{"db/create-db.sql"});

    public static void insert(Goods goods) {
        jdbcTemplate.update("INSERT INTO Goods VALUES (?, ?)", goods.getCode(), goods.getQuantity());
    }

    public static Integer getGoodsCount(int good_id) {
        int quantity = -1;
        try {
            quantity = jdbcTemplate.queryForObject("SELECT quantity FROM GOODS WHERE id = ?", Integer.class, good_id);
        } catch (DataAccessException ignored) {}
        return quantity;
    }

    public static List<Goods> showAllGoods() {
        List<Goods> goods = null;
        try {
            goods = jdbcTemplate.query("SELECT * FROM Goods",
                    (rs, rowNum) -> new Goods(rs.getInt("id"), rs.getInt("quantity")));
        } catch (DataAccessException ignored) {}
        return goods;
    }

    public static void addNewRequest(Request request) {
        int type_id = jdbcTemplate.queryForObject("SELECT id FROM OrderTypeList WHERE type = ?",
                Integer.class, request.getType().toString());
        int status_id = jdbcTemplate.queryForObject("SELECT id FROM StatusList WHERE status = ?",
                Integer.class, "in progress");
        Timestamp time = new Timestamp(System.currentTimeMillis());
        jdbcTemplate.update(
                "INSERT INTO Request VALUES (id, user_id, goods_id, quantity, type, date, attempts_count, status)", request.getId(),
                request.getUserId(), request.getUniqueCode(), request.getAmount(), type_id,time, 0, status_id);
    }

    public static void payOrder(long id) {
        updateOrderType(id, "paid", "done");
    }

    public static void cancelOrder(long id) {
        updateOrderType(id, "canceled", "canceled");
    }

    private static void updateOrderType(long id, String type, String status) {
        int type_id = jdbcTemplate.queryForObject("SELECT id FROM OrderTypeList WHERE type = ?",
                Integer.class, type);
        int status_id = jdbcTemplate.queryForObject("SELECT id FROM StatusList WHERE status = ?",
                Integer.class, status);
        jdbcTemplate.update("UPDATE Request SET type = ? WHERE id = ?", type_id, id);
        jdbcTemplate.update("UPDATE Request SET status = ? WHERE id = ?", status_id, id);
        jdbcTemplate.update("UPDATE Request SET attempts_count = ? WHERE id = ?", 0, id);
    }
}
