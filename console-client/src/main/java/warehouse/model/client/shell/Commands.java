package warehouse.model.client.shell;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
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

    @CliCommand(value = "sign up", help = "Sign up new user")
    public String signUp(
            @CliOption(key = {"l"}, mandatory = true, help = "User login") String login,
            @CliOption(key = {"p"}, mandatory = true, help = "User password") String password)
    {
        ResponseEntity<Integer> userId;
        try {
            userId = restTemplate.postForEntity(serverAddress + "/new_user", new User(login, password), Integer.class);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode()== HttpStatus.INTERNAL_SERVER_ERROR) {
                return ERROR_MESSAGE;
            }
            if (e.getStatusCode()== HttpStatus.BAD_REQUEST) {
                return "This login already exists.";
            }
            return ERROR_MESSAGE;
        } catch (RestClientException e) {
            return ERROR_MESSAGE;
        }
        return "New user id is " + userId.getBody();
    }

    @CliCommand(value = "login", help = "User authentication")
    public String signIn(@CliOption(key = {"l"}, mandatory = true, help = "User login") String login,
                         @CliOption(key = {"p"}, mandatory = true, help = "User password") String password)
    {
        ResponseEntity<Integer> userId;
        try {
            userId = restTemplate.getForEntity(serverAddress + "/user_existence/" + login + "/" + password, Integer.class);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode()== HttpStatus.INTERNAL_SERVER_ERROR) {
                return ERROR_MESSAGE;
            }
            if (e.getStatusCode()== HttpStatus.BAD_REQUEST) {
                return "The username or password is incorrect.";
            }
            return ERROR_MESSAGE;
        } catch (RestClientException e) {
            return ERROR_MESSAGE;
        }
        return "User Id is " + userId.getBody();
    }


    @CliCommand(value = "get", help = "Checking the number of available product with specified code")
    public String getCount(
            @CliOption(key = {"code"}, mandatory = true, help = "Unique product code") int uniqueCode)
    {
        ResponseEntity<Integer> count;
        try {
            count = restTemplate.getForEntity(serverAddress + "/goods/" + uniqueCode, Integer.class);
        } catch (RestClientException e) {
            return ERROR_MESSAGE;
        }
        if (count == null || count.getStatusCode() != HttpStatus.OK) {
            return ERROR_MESSAGE;
        }
        return "Now available " + count.getBody() + " items.";
    }

    @CliCommand(value = "show all", help = "Show all available goods")
    public String showAll()
    {
        List<Goods> allGoods;
        try {
            ObjectMapper mapper = new ObjectMapper();
            ResponseEntity<JsonNode> goods = restTemplate.getForEntity(serverAddress + "/all_goods", JsonNode.class);
            if (goods == null || goods.getStatusCode() != HttpStatus.OK) {
                return ERROR_MESSAGE;
            }
            allGoods = null;
            try {
                allGoods = mapper.readValue(mapper.treeAsTokens(goods.getBody()), new TypeReference<List<Goods>>() {});
            } catch (IOException ignored) {}
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
        ResponseEntity<Long> orderId;
        try {
            orderId = restTemplate.getForEntity(serverAddress + "/new_order_number", Long.class);
            if (orderId == null || orderId.getStatusCode() != HttpStatus.CREATED) {
                return ERROR_MESSAGE;
            }
            ResponseEntity<Long> isSuccessful = restTemplate.postForEntity(serverAddress + "/book", new Request(orderId.getBody(), id, unique_code, amount), Long.class);
            if (isSuccessful == null || isSuccessful.getStatusCode() != HttpStatus.OK) {
                return ERROR_MESSAGE;
            }
        } catch (RestClientException e) {
            return ERROR_MESSAGE;
        }
        return "Id of your request is " + orderId.getBody() + ". Your order in progress.";
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
            ResponseEntity<JsonNode> requestsJson = restTemplate.getForEntity(serverAddress + "/all_user_orders/" + id, JsonNode.class);
            if (requestsJson == null || requestsJson.getStatusCode() != HttpStatus.OK) {
                return ERROR_MESSAGE;
            }
            requests = mapper.readValue(mapper.treeAsTokens(requestsJson.getBody()), new TypeReference<List<Request>>() {});
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

    @CliCommand(value = "reset", help = "Reset attempts count of all requests to 0")
    public String resetAttemptsCount(@CliOption(key = {"p"}, mandatory = true, help = "Admin password") String password) {
        try {
            restTemplate.put(serverAddress + "/reset/" + password, null);
        } catch (RestClientException e) {
            return ERROR_MESSAGE;
        }
        return "Attempts were changed.";
    }
}
