package warehouse.model.client.shell;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
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
    private String ERROR_MESSAGE = "Information isn't available now. Please, try again later.";

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

    @CliCommand(value = "set wh address", help = "Set new wh server address")
    public String setWHSAddress(
            @CliOption(key = {"address ", ""}, mandatory = true, help = "WH server address") String address)
    {
        try {
            restTemplate.put(serverAddress + "/address/" + address, null);
        } catch (RestClientException e) {
            return "Address was't changed. Please, try again later.";
        }
        return "Address was successfully changed";
    }

    @CliCommand(value = "sign up", help = "Sign up new user")
    public String signUp(
            @CliOption(key = {"l"}, mandatory = true, help = "User login") String login,
            @CliOption(key = {"p"}, mandatory = true, help = "User password") String password)
    {
        int userId;
        try {
            userId = restTemplate.postForObject(serverAddress + "/user", new User(login, password), Integer.class);
        } catch (RestClientException e) {
            return "Fail during connection to server";
        }
        if (userId == -1) {
            return "Fail during adding to db";
        }
        return "New user id is " + userId;
    }

    @CliCommand(value = "login", help = "User authentication")
    public String signIn(@CliOption(key = {"l"}, mandatory = true, help = "User login") String login,
                         @CliOption(key = {"p"}, mandatory = true, help = "User password") String password)
    {
        int userId;
        try {
            userId = restTemplate.getForObject(serverAddress + "/check_user/" + login + "/" + password, Integer.class);
        } catch (RestClientException e) {
            return ERROR_MESSAGE;
        }
        if (userId == -1) {
            return "The username or password you entered is incorrect.";
        }
        return "User Id is " + userId;
    }


    @CliCommand(value = "get", help = "Checking the number of available product with specified code")
    public String getCount(
            @CliOption(key = {"code"}, mandatory = true, help = "Unique product code") int uniqueCode)
    {
        int count;
        try {
            count = restTemplate.getForObject(serverAddress + "/goods/" + uniqueCode, Integer.class);
        } catch (RestClientException e) {
            return ERROR_MESSAGE;
        }
        if (count == -1) {
            return ERROR_MESSAGE;
        }
        return "Now available " + count + " items.";
    }

    @CliCommand(value = "show all", help = "Show all available goods")
    public String showAll()
    {
        List<Goods> allGoods;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode goods = restTemplate.getForObject(serverAddress + "/all_goods", JsonNode.class);
            if (goods == null) {
                return ERROR_MESSAGE;
            }
            allGoods = null;
            try {
                allGoods = mapper.readValue(mapper.treeAsTokens(goods), new TypeReference<List<Goods>>() {});
            } catch (IOException ignored) {
            }
        } catch (RestClientException e) {
            return ERROR_MESSAGE;
        }
        StringBuilder sb = new StringBuilder();
        if (allGoods == null) {
            return ERROR_MESSAGE;
        }
        sb.append("Total: ").append(allGoods.size()).append(" items:\n");
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
        long orderId;
        try {
            orderId = restTemplate.getForObject(serverAddress + "/new_order", Long.class);
            restTemplate.postForObject(serverAddress + "/book", new Request(orderId, id, unique_code, amount), Long.class);
        } catch (RestClientException e) {
            return ERROR_MESSAGE;
        }
        return "Id of your request is " + orderId + ". Your order in progress.";
    }

    @CliCommand(value = "pay", help = "Pay the order")
    public String pay(@CliOption(key = {"id"}, mandatory = true, help = "Order id") long id) {
        restTemplate.put(serverAddress + "/payment/" + id, null);
        return "Your order #" + id + " was paid.";
    }

    @CliCommand(value = "cancel", help = "Cancel the order")
    public String cancel(@CliOption(key = {"id"}, mandatory = true, help = "Order id") long id) {
        restTemplate.put(serverAddress + "/cancellation/" + id, null);
        return "Your order #" + id + " was canceled.";
    }

    @CliCommand(value = "user orders", help = "All user orders wits status")
    public String allUserOrders(@CliOption(key = {"id"}, mandatory = true, help = "User id") long id) {
        List<Request> requests;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode requestsJson = restTemplate.getForObject(serverAddress + "/all_orders/" + id, JsonNode.class);
            requests = mapper.readValue(mapper.treeAsTokens(requestsJson), new TypeReference<List<Request>>() {});
        } catch (RestClientException | IOException e) {
            return ERROR_MESSAGE + ": " + e;
        }
        if (requests == null) {
            return ERROR_MESSAGE;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Total: ").append(requests.size()).append(" orders:\n");
        for (Request item : requests) {
            sb.append(item.toString()).append("\n");
        }
        return sb.toString();
    }
}
