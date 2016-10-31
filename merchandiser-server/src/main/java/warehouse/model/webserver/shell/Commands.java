package warehouse.model.webserver.shell;

import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;
import warehouse.model.webserver.db.JDBCTemplate;

@Component
public class Commands implements CommandMarker {
    private static ConfigurableApplicationContext context;

    public static void setApplicationContext(ConfigurableApplicationContext newContext) {
        if (context == null) {
            context = newContext;
        }
    }

    @CliCommand(value = "stop", help = "Close shell and shutdown the server")
    public void stop() {
        System.exit(SpringApplication.exit(context, (ExitCodeGenerator) () -> 0));
        ((EmbeddedDatabase)JDBCTemplate.getInstance().getDataSource()).shutdown();
    }
}
