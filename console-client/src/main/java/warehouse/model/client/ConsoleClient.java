package warehouse.model.client;

import org.slf4j.Logger;
import org.springframework.shell.Bootstrap;
import org.springframework.shell.core.JLineShellComponent;
import warehouse.model.loggers.Loggers;

import java.io.IOException;

public class ConsoleClient {
    private static final Logger log = Loggers.getConsoleLogger(ConsoleClient.class);

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            printHelp("Required to run parameters aren't specified. ");
            return;
        }
        String serverAddress = args[0];
        String[] addr = serverAddress.split(":");
        if (addr.length != 2) {
            printHelp("Specified server address isn't available. ");
            return;
        }

        new ConsoleClient().startCli(serverAddress);
    }

    private void startCli(String serverAddress) {
        Bootstrap bootstrap = new Bootstrap();
        JLineShellComponent shell = bootstrap.getJLineShellComponent();
        shell.start();
        shell.executeCommand("set address " + serverAddress);
    }

    private static void printHelp(String message) {
        log.error(message);
        log.info("To start cli run: " +
                "\n\tjava -jar console-client-<version>.jar <address>:<port>");
    }
}
