package warehouse.model.webserver;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.shell.Bootstrap;
import org.springframework.shell.core.JLineShellComponent;
import warehouse.model.loggers.Loggers;
import warehouse.model.webserver.shell.Commands;

@SpringBootApplication
@EnableScheduling
public class ServerLauncher {
    private static final Logger log = Loggers.getConsoleLogger(ServerLauncher.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            log.error("Invalid parameters. To start server run: " +
                    "\n\tjava -jar warehouse-server-<version>.jar <port>");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            log.error("Invalid port number. ");
            return;
        }
        new ServerLauncher().startServer(port);
    }

    private void startServer(int port) {
        System.getProperties().put("server.port", port);

        ConfigurableApplicationContext context = new SpringApplicationBuilder()
                .sources(ServerLauncher.class)
                .run();

        Commands.setApplicationContext(context);

        Bootstrap bootstrap = new Bootstrap();
        JLineShellComponent shell = bootstrap.getJLineShellComponent();
        shell.start();
    }
}