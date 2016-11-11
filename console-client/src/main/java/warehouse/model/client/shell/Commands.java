package warehouse.model.client.shell;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import warehouse.model.entities.Goods;
import warehouse.model.entities.Request;
import warehouse.model.entities.User;

import java.io.IOException;
import java.util.List;

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

    @CliCommand(value = "set wh address", help = "Set new server address")
    public void setWHSAddress(
            @CliOption(key = {"address ", ""}, mandatory = true, help = "WH server address") String address)
    {
        restTemplate.put(serverAddress + "/address/" + address, null);
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

    @CliCommand(value = "show all", help = "Show all available goods")
    public String showAll()
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode goods = restTemplate.getForObject(serverAddress + "/all_goods", JsonNode.class);
        List<Goods> allGoods = null;
        try {
            allGoods =  mapper.readValue(mapper.treeAsTokens(goods), new TypeReference<List<Goods>>(){});
        } catch (IOException ignored) {}
        StringBuilder sb = new StringBuilder();
        if (allGoods == null) {
            return "Information isn't available now. Please, try again later.";
        }
        for (Goods item : allGoods) {
            sb.append(item.toString()).append("\n");
        }
        return sb.toString();
    }

    @CliCommand(value = "order", help = "Order the product")
    public String book(
            @CliOption(key = {"u"}, mandatory = true, help = "Id of user") int id,
            @CliOption(key = {"id"}, mandatory = true, help = "Product unique code") int unique_code,
            @CliOption(key = {"num"}, mandatory = true, help = "Number of products that user want to book") int amount)
    {
        long orderId = restTemplate.postForObject(serverAddress + "/book", new Request(id, unique_code,
                        amount, Request.RequestType.BOOKED), Long.class);
        return "Id of your request is " + orderId;
    }
}
