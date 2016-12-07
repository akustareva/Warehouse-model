package warehouse.model.merchandiser.webserver;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import warehouse.model.entities.Order;
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
    private static String DONE_STATUS = "done";
    private RestTemplate restTemplate = new RestTemplate();
    private String whServerAddress = bundle.getString("wh.server.default.address");

    @RequestMapping(value = "/goods/{good_id}", method = RequestMethod.GET)
    public ResponseEntity<Integer> checkRequest(@PathVariable int good_id) {
        try {
            return restTemplate.getForEntity(whServerAddress + "/goods/" + good_id, Integer.class);
        } catch (RestClientException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/all_goods", method = RequestMethod.GET)
    public ResponseEntity<JsonNode> showRequest() {
        try {
            ResponseEntity<JsonNode> goods = restTemplate.getForEntity(whServerAddress + "/all_goods", JsonNode.class);
            SQLExecutor.updateGoodsTable(goods.getBody());
            return goods;
        } catch (RestClientException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/all_user_orders/{id}", method = RequestMethod.GET)
    public ResponseEntity<List<Order>> showAllOrders(@PathVariable int id) {
        List<Order> orders = SQLExecutor.allUserOrders(id);
        if (orders == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return  new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @RequestMapping(value = "/new_order_number", method = RequestMethod.GET)
    public ResponseEntity<Long> getNewOrderId() {
        number = (number + 1) % MOD;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String tmp = dateFormat.format(new Date()) + number;
        return new ResponseEntity<>(Long.parseLong(tmp), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/new_user", method = RequestMethod.POST)
    public ResponseEntity<Integer> userSignUp(@RequestBody User user) {
        Integer userId = SQLExecutor.insert(user);
        if (userId == -1) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else if (userId == -2) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(userId, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/user_existence/{login}/{password}", method = RequestMethod.GET)
    public ResponseEntity<Integer> userSignIn(@PathVariable String login, @PathVariable String password) {
        Integer userId = SQLExecutor.checkUser(new User(login, password));
        if (userId == -1) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else if (userId == -2) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(userId, HttpStatus.OK);
    }

    @RequestMapping(value = "/book", method = RequestMethod.POST)
    public ResponseEntity bookRequest(@RequestBody Request request) {
        HttpStatus status = SQLExecutor.addNewRequest(request);
        if (status != HttpStatus.OK) {
            return new ResponseEntity(status);
        }
        try {
            ResponseEntity response = restTemplate.postForEntity(whServerAddress + "/book", request, Object.class);
            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                SQLExecutor.updateOrderStatus(request.getId(), DONE_STATUS);
            }
        } catch (RestClientException | DataAccessException ignored) {}
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/payment/{orderId}", method = RequestMethod.PUT)
    public void paymentRequest(@PathVariable long orderId) {
        SQLExecutor.payOrder(orderId);
        try {
            restTemplate.put(whServerAddress + "/payment/" + orderId, null);
            SQLExecutor.updateOrderStatus(orderId, DONE_STATUS);
        } catch (RestClientException ignore) {}
    }

    @RequestMapping(value = "/cancellation/{orderId}", method = RequestMethod.PUT)
    public void cancelRequest(@PathVariable long orderId) {
        SQLExecutor.cancelOrder(orderId);
        try {
            restTemplate.put(whServerAddress + "/cancellation/" + orderId, null);
        } catch (RestClientException ignore) {}
    }

    @RequestMapping(value = "/reset/{adminPassword}", method = RequestMethod.PUT)
    public ResponseEntity resetAttemptsCount(@PathVariable String adminPassword) {
        if (adminPassword.equals(bundle.getString("admin.password"))) {
            HttpStatus status = SQLExecutor.resetAttemptsCount();
            return new ResponseEntity(status);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
}
