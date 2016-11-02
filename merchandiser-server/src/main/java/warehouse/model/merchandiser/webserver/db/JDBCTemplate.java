package warehouse.model.merchandiser.webserver.db;

import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.SQLException;

public class JDBCTemplate {
    private static final Logger log = LoggerFactory.getLogger(JDBCTemplate.class);
    private static JdbcTemplate jdbcTemplate;

    private static DataSource DataSource() {
//        return new EmbeddedDatabaseBuilder()
//                .setName("Warehouse.merchandiser.database")
//                .setType(H2)
//                .addScript("db/create-db.sql")
//                .build();
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:./database/warehouse");
        dataSource.setUser("warehouse");
        dataSource.setPassword("");
        return dataSource;
    }

    public static JdbcTemplate getInstance() {
        if (jdbcTemplate == null) {
            DataSource dataSource = DataSource();
            jdbcTemplate = new JdbcTemplate(dataSource);
            try {
                ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("db/create-db.sql"));
            } catch (SQLException e) {
                log.error("Error during loading db: ", e);
            }
        }
        return jdbcTemplate;
    }
}
