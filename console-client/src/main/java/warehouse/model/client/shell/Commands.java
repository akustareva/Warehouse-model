package warehouse.model.client.shell;

import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import warehouse.model.entities.OrderRequest;
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
        address += "mh";
        this.serverAddress = address;
    }

    @CliCommand(value = "sign up", help = "Sign up new user")
    public String signUp(
            @CliOption(key = {"login"}, mandatory = true, help = "User login") String login,
            @CliOption(key = {"password"}, mandatory = true, help = "User password") String password)
    {
        int userId = restTemplate.postForObject(serverAddress + "/user", new User(login, password), Integer.class);
        if (userId == -1) {
            return "Fail";
        }
        return "New user id is " + userId;
    }

    @CliCommand(value = "get", help = "Checking the number of available product with specified code")
    public String getCount(
            @CliOption(key = {"code"}, mandatory = true, help = "Unique product code") int uniqueCode)
    {
        int count = restTemplate.getForObject(serverAddress + "/goods/" + uniqueCode, Integer.class);
        if (count == -1) {
            return "Information isn't available now. Please, try again later.";
        }
        return "Now available " + count + " items.";
    }

    @CliCommand(value = "book", help = "Book the product")
    public String book(
            @CliOption(key = {"u"}, mandatory = true, help = "Id of user") int id,
            @CliOption(key = {"id"}, mandatory = true, help = "Product unique code") int unique_code,
            @CliOption(key = {"num"}, mandatory = true, help = "Number of products that user want to book") int amount)
    {
        long orderId = restTemplate.postForObject(serverAddress + "/order", new OrderRequest(id, unique_code, amount,
                OrderRequest.RequestType.BOOK), Long.class);
        return "Id of your request is " + orderId;
    }

    @CliCommand(value = "buy", help = "Buy the product")
    public String buy(
            @CliOption(key = {"u"}, mandatory = true, help = "Id of user") int id,
            @CliOption(key = {"id"}, mandatory = true, help = "Product unique code") int unique_code,
            @CliOption(key = {"num"}, mandatory = true, help = "Number of products that user want to book") int amount)
    {
        long orderId = restTemplate.postForObject(serverAddress + "/order", new OrderRequest(id, unique_code, amount,
                OrderRequest.RequestType.BUY), Long.class);
        return "Id of your request is " + orderId;
    }

    @CliCommand(value = "cancel", help = "Cancel the order")
    public void cancel(
            @CliOption(key = {"id"}, mandatory = true, help = "Id of order") int id)
    {
        restTemplate.put(serverAddress + "/cancellation/" + id, null);
    }
}
