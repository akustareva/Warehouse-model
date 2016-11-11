package warehouse.model.webserver.db;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import warehouse.model.db.JDBCTemplate;
import warehouse.model.entities.Goods;

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
                    (rs, rowNum) -> new Goods(rs.getInt("id"), rs.getInt("quantity"))); // TODO: does't work. why?
        } catch (DataAccessException ignored) {}
        return goods;
    }
}
