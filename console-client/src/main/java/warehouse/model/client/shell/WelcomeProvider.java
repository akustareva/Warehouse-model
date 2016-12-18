package warehouse.model.client.shell;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WelcomeProvider extends DefaultBannerProvider {

    @Override
    public String getBanner() {
        return "Version:" + this.getVersion();
    }

    @Override
    public String getVersion() {
        return "1.0-SNAPSHOT";
    }

    public String getWelcomeMessage() {
        return "Welcome! Press TUB to see available commands or use command \"help\" to see more information about them. ";
    }
}
