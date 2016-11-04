package warehouse.model.merchandiser.webserver;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import warehouse.model.entities.Request;
import warehouse.model.entities.User;
import warehouse.model.merchandiser.webserver.db.SQLExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/mh")
public class Queries {
    private static final int MOD = 106033;
    private static int number = 0;
    private RestTemplate restTemplate = new RestTemplate();
    private String whServerAddress;

    @RequestMapping(value = "/goods/{good_id}", method = RequestMethod.GET)
    public Integer checkRequest(@PathVariable int good_id) {
        return restTemplate.getForObject(whServerAddress + "/goods/" + good_id, Integer.class);
    }

    @RequestMapping(value = "/all_goods", method = RequestMethod.GET)
    public JsonNode showRequest() {
        return restTemplate.getForObject(whServerAddress + "/all_goods", JsonNode.class);
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

    @RequestMapping(value = "/book", method = RequestMethod.POST)
    public Long bookRequest(@RequestBody Request request) {
        Long id = getNewOrderId();
        SQLExecutor.addNewRequest(request, id);
        // TODO: send request in warehouse server. Returns order_id.
        return id;
    }

    @RequestMapping(value = "/payment/{order_id}", method = RequestMethod.PUT)
    public void paymentRequest(@PathVariable int order_id) {
        // TODO: send request in warehouse server.
    }

    @RequestMapping(value = "/cancellation/{order_id}", method = RequestMethod.PUT)
    public void cancelRequest(@PathVariable int order_id) {
        // TODO: send request in warehouse server.
    }
}
