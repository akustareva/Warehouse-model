package warehouse.model.merchandiser.webserver.shell;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.shell.support.util.OsUtils;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WelcomeProvider extends DefaultBannerProvider {

    @Override
    public String getBanner() {
        StringBuilder buf = new StringBuilder();
        buf.append("===================================").append(OsUtils.LINE_SEPARATOR);
        buf.append("*                                 *").append(OsUtils.LINE_SEPARATOR);
        buf.append("*      Welcome to merchandiser    *").append(OsUtils.LINE_SEPARATOR);
        buf.append("*                                 *").append(OsUtils.LINE_SEPARATOR);
        buf.append("===================================").append(OsUtils.LINE_SEPARATOR);
        buf.append("Version:").append(this.getVersion());
        return buf.toString();
    }

    @Override
    public String getVersion() {
        return "1.0-SNAPSHOT";
    }

    public String getWelcomeMessage() {
        return "Welcome to Merchandiser Service. Press TUB to see available commands or " +
                "use command \"help\" to see more information about them. " +
                "Please use command \"stop\" to safely shut down the server.";
    }
}
