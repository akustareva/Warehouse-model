package warehouse.model.webserver;

import warehouse.model.webserver.entities.Goods;
import warehouse.model.webserver.entities.GoodsDisplay;
import warehouse.model.webserver.entities.StatusList;
import warehouse.model.webserver.entities.Warehouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RestController
@SpringBootApplication
@RequestMapping("/warehouse")
public class ServerApplication {
    private static Logger log = LoggerFactory.getLogger(ServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @RequestMapping(value = "/buy/articul_id={articul_id}&warehouse={warehouse_name}&user={user_id}&quantity={quantity}")
    public String buy(
        @PathVariable int articul_id, @PathVariable String warehouse_name,
        @PathVariable int user_id, @PathVariable int quantity)
    {
        log.info("Access /buy");
        JdbcTemplate jdbcTemplate = SQLExecuter.getExecuter();

        /* получаем ид магазина по названию */
        int warehouse_id = jdbcTemplate.queryForObject(
                "select warehouse_id from Warehouse where name=?",
                Integer.class,
                warehouse_name
        );

        /* получаем ид свободного статуса */
        int free_status = jdbcTemplate.queryForObject(
                "select status_id from StatusList where description=?",
                Integer.class,
                "Free"
        );
        log.info("free status is " + free_status);

        /* получаем ид занятого статуса */
        int reserved_status = jdbcTemplate.queryForObject(
                "select status_id from StatusList where description=?",
                Integer.class,
                "Reserved"
        );
        log.info("reserved status is " + free_status);

        /* смотрим, есть ли свободные товары в этом магазине с количеством >= */
        int count = jdbcTemplate.queryForObject(
                "select count(*) from GoodsDisplay where warehouse_id=? and articul_id=? and status_id=? and quantity >=?",
                Integer.class, warehouse_id, articul_id, free_status, quantity);

        /* если свободных нет, выходим */
        if (count == 0){
            return "You cant order it right now.";
        }

        /* получаем количество свободных товаров в таблице */
        int table_quantity = jdbcTemplate.queryForObject(
                "select quantity from GoodsDisplay where warehouse_id=? and articul_id=? and status_id=?",
                Integer.class, warehouse_id, articul_id, free_status);

        /* проверяем, есть ли в таблице этот товар с требующимся нам новым статусом именно для этого пользователя */
        int is_duplicate = jdbcTemplate.queryForObject(
                "select count(*) from GoodsDisplay where warehouse_id=? and articul_id=? and status_id=? and user_id=?",
                Integer.class, warehouse_id, articul_id, reserved_status, user_id);

        if (is_duplicate == 0){
            /* если занятых идентичных товаров нет, то создаём новую запись */
            jdbcTemplate.update("insert into GoodsDisplay (articul_id, warehouse_id, status_id, user_id, quantity)" +
                            " values (?, ?, ?, ?, ?)",
                    articul_id, warehouse_id, reserved_status, user_id, quantity);
        }
        else{
            /* иначе получаем quantity в имеющейся записи и увеличиваем значение этого поля на нужное число */
            int duplicate_quantity = jdbcTemplate.queryForObject(
                    "select quantity from GoodsDisplay where warehouse_id=? and articul_id=? and status_id=? and user_id=?",
                    Integer.class, warehouse_id, articul_id, reserved_status, user_id);
            jdbcTemplate.update(
                    "update GoodsDisplay set quantity=? where warehouse_id=? and articul_id=? and status_id=? and user_id=?",
                    duplicate_quantity + quantity, warehouse_id, articul_id, reserved_status, user_id);
        }

        /* если quantity свободных равно числу, которое мы запросили, то удаляем запись о свободных товарах
        *  иначе из числа свободных просто вычитаем количество забранных */
        if(table_quantity == quantity){
            jdbcTemplate.update(
                    "delete from GoodsDisplay where articul_id=? and warehouse_id=? and status_id=? and user_id=?",
                    articul_id, warehouse_id, free_status, 0);
        }
        else{
            jdbcTemplate.update(
                    "update GoodsDisplay set quantity=? where warehouse_id=? and articul_id=? and status_id=? and user_id=?",
                    table_quantity - quantity, warehouse_id, articul_id, free_status, 0);
        }

        return "Operation was successfully completed.";
    }

    @RequestMapping(value = "/get/{table}")
    public String get(@PathVariable("table") String table) {
        log.info("Access /get/" + table);

        StringBuilder sb = new StringBuilder();
        JdbcTemplate jdbcTemplate = SQLExecuter.getExecuter();
        List results = null;

        switch (table){
            case "GoodsDisplay":
                results = jdbcTemplate.query(
                    "select * from " + table,
                    new RowMapper<GoodsDisplay>() {
                        @Override
                        public GoodsDisplay mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return new GoodsDisplay(rs.getLong("articul_id"), rs.getLong("warehouse_id"),
                                    rs.getLong("status_id"), rs.getLong("quantity"), rs.getLong("user_id"));
                        }
                    }
                );
                break;
            case "Goods":
                results = jdbcTemplate.query(
                    "select * from " + table,
                    new RowMapper<Goods>() {
                        @Override
                        public Goods mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return new Goods(rs.getLong("articul_id"), rs.getString("merchandise_name"), rs.getString("description"));
                        }
                    }
                );
                break;
            case "Warehouse":
                results = jdbcTemplate.query(
                    "select * from " + table,
                    new RowMapper<Warehouse>() {
                        @Override
                        public Warehouse mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return new Warehouse(rs.getLong("warehouse_id"), rs.getString("name"), rs.getString("address"), rs.getString("telephone"));
                        }
                    }
                );
                break;
            case "StatusList":
                results = jdbcTemplate.query(
                    "select * from " + table,
                    new RowMapper<StatusList>() {
                        @Override
                        public StatusList mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return new StatusList(rs.getLong("status_id"), rs.getString("description"));
                        }
                    }
                );
                break;
            default:
                sb.append("Empty result.");
                break;
        }
        if (results != null) {
            for (Object item : results) {
                sb.append(item.toString());
                sb.append("<br>");
            }
        }
        return sb.toString();
    }

    @RequestMapping(value = "/")
    public String home() {
        log.info("Access /");
        return "Hi!";
    }
}