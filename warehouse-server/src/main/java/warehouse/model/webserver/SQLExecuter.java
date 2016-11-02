package warehouse.model.webserver;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.core.JdbcTemplate;


public class SQLExecuter {

    public static JdbcTemplate getExecuter() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:./database/warehouse");
        ds.setUser("sa");
        ds.setPassword("");
        return new JdbcTemplate(ds);
    }
}

