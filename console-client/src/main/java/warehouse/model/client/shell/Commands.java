package warehouse.model.client.shell;

import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import warehouse.model.entities.User;

@Component
public class Commands implements CommandMarker {
    private RestTemplate restTemplate = new RestTemplate();
    private String serverAddress;

    @CliCommand(value = "set address", help = "Set new server address")
    public void setServerAddress(
            @CliOption(key = {"address ", ""}, mandatory = true, help = "Server address") String address)
    {
        if (!(address.startsWith("http") || address.startsWith("https"))) {
            address = "http://" + address;
        }
        if (!address.endsWith("/")) {
            address += "/";
        }
        address += "warehouse/";
        this.serverAddress = address;
    }

    @CliCommand(value = "sign up", help = "Sign up new user")
    public void signUp(
            @CliOption(key = {"login", ""}, mandatory = true, help = "User login") String login,
            @CliOption(key = {"password", ""}, mandatory = true, help = "User password") String password)
    {
        restTemplate.put(serverAddress + "sign_up", new User(login, password));
    }

    @CliCommand(value = "get", help = "Checking the number of available product with specified code")
    public String get(
            @CliOption(key = {"code", ""}, mandatory = true, help = "Unique product code") int uniqueCode)
    {
        return restTemplate.postForObject(serverAddress + "get", uniqueCode, String.class);
    }
}
