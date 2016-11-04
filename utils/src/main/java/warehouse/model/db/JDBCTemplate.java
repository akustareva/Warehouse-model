package warehouse.model.db;

import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class JDBCTemplate {
    private static final Logger log = LoggerFactory.getLogger(JDBCTemplate.class);
    private static JdbcTemplate jdbcTemplate;

    private static DataSource getDataSource(String url, String user, String password) {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    public static JdbcTemplate getInstance(String url, String user, String password, String[] scripts) {
        if (jdbcTemplate == null) {
            DataSource dataSource = getDataSource(url, user, password);
            Connection dbConnection = null;
            jdbcTemplate = new JdbcTemplate(dataSource);
            try {
                dbConnection = dataSource.getConnection();
                for (String script : scripts) {
                    ScriptUtils.executeSqlScript(dbConnection, new ClassPathResource(script));
                }
            } catch (SQLException e) {
                log.error("Error during loading db: ", e);
            } finally {
                try {
                    if (dbConnection != null) {
                        dbConnection.close();
                    }
                } catch (SQLException ignored) {}
            }
        }
        return jdbcTemplate;
    }

    public static JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}
