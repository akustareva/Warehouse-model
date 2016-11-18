package warehouse.model.merchandiser.webserver;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import warehouse.model.entities.Request;
import warehouse.model.entities.User;
import warehouse.model.merchandiser.webserver.db.SQLExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@RestController
@RequestMapping("/mh")
public class Queries {
    private static ResourceBundle bundle = ResourceBundle.getBundle("mh", Locale.US);
    private static final int MOD = 106033;
    private static int number = 0;
    private RestTemplate restTemplate = new RestTemplate();
    private String whServerAddress = bundle.getString("wh.server.default.address");

    @RequestMapping(value = "/goods/{good_id}", method = RequestMethod.GET)
    public Integer checkRequest(@PathVariable int good_id) {
        try {
            return restTemplate.getForObject(whServerAddress + "/goods/" + good_id, Integer.class);
        } catch (RestClientException e) {
            return -1;
        }
    }

    @RequestMapping(value = "/all_goods", method = RequestMethod.GET)
    public JsonNode showRequest() {
        try {
            return restTemplate.getForObject(whServerAddress + "/all_goods", JsonNode.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    @RequestMapping(value = "/all_orders/{id}", method = RequestMethod.GET)
    public List<Request> showAllOrders(@PathVariable int id) {
        return  SQLExecutor.allUserOrders(id);
    }

    @RequestMapping(value = "/new_order", method = RequestMethod.GET)
    public Long getNewOrderId() {
        number = (number + 1) % MOD;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String tmp = dateFormat.format(new Date()) + number;
        return Long.parseLong(tmp);
    }

    @RequestMapping(value = "/address/{address}", method = RequestMethod.PUT)
    public void setAddress(@PathVariable String address) {
        if (!(address.startsWith("http") || address.startsWith("https"))) {
            address = "http://" + address;
        }
        if (!address.endsWith("/")) {
            address += "/";
        }
        address += "wh";
        this.whServerAddress = address;
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public Integer userSignUp(@RequestBody User user) {
        return SQLExecutor.insert(user);
    }

    @RequestMapping(value = "/check_user/{login}/{password}", method = RequestMethod.GET)
    public Integer userSignIn(@PathVariable String login, @PathVariable String password) {
        return SQLExecutor.checkUser(new User(login, password));
    }

    @RequestMapping(value = "/book", method = RequestMethod.POST)
    public Long bookRequest(@RequestBody Request request) {
        SQLExecutor.addNewRequest(request);
        try {
            restTemplate.postForObject(whServerAddress + "/book", request, Long.class);
        } catch (RestClientException e) {
            return -1L;
        }
        return request.getId();
    }

    @RequestMapping(value = "/payment/{order_id}", method = RequestMethod.PUT)
    public void paymentRequest(@PathVariable long order_id) {
        SQLExecutor.payOrder(order_id);
        restTemplate.put(whServerAddress + "/payment/" + order_id, null);
    }

    @RequestMapping(value = "/cancellation/{order_id}", method = RequestMethod.PUT)
    public void cancelRequest(@PathVariable long order_id) {
        SQLExecutor.cancelOrder(order_id);
        restTemplate.put(whServerAddress + "/cancellation/" + order_id, null);
    }
}
