package warehouse.model.webserver.db;

import org.springframework.jdbc.core.JdbcTemplate;
import warehouse.model.db.JDBCTemplate;

public class SQLExecutor {
    private static JdbcTemplate jdbcTemplate = JDBCTemplate.getInstance("jdbc:h2:./database/wh", "wh", "",
            new String[]{"db/create-db.sql"});
}
