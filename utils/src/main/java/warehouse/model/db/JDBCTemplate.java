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
import java.util.HashMap;
import java.util.Map;

public class JDBCTemplate {
    private static final Logger log = LoggerFactory.getLogger(JDBCTemplate.class);
    private static Map<Type, JdbcTemplate> templates = new HashMap<>();

    private static DataSource getDataSource(String url, String user, String password) {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    public static JdbcTemplate getInstance(Type type, String url, String user, String password, String[] scripts) {
        DataSource dataSource = getDataSource(url, user, password);
        Connection dbConnection = null;
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        templates.put(type, jdbcTemplate);
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
        return jdbcTemplate;
    }

    public static JdbcTemplate getJdbcTemplate(Type type) {
        return templates.get(type);
    }

    public enum Type {
        WH,
        MH
    }
}
