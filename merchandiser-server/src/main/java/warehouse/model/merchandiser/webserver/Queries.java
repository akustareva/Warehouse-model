package warehouse.model.merchandiser.webserver;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import warehouse.model.entities.OrderRequest;
import warehouse.model.entities.User;
import warehouse.model.merchandiser.webserver.db.SQLExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/mh")
public class Queries {
    private static final int MOD = 106033;
    private static int number = 0;

    @RequestMapping(value = "/goods/{good_id}", method = RequestMethod.GET)
    public Integer checkRequest(@PathVariable int good_id) {
        // TODO: send request in warehouse server. Returns count or -1.
        return -1;
    }

    @RequestMapping(value = "/new_order", method = RequestMethod.GET)
    public Long getNewOrderId() {
        number = (number + 1) % MOD;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String tmp = dateFormat.format(new Date()) + number;
        return Long.parseLong(tmp);
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public Integer userSignUp(@RequestBody User user) {
        return SQLExecutor.insert(user);
    }

    @RequestMapping(value = "/order", method = RequestMethod.POST)
    public Long orderRequest(@RequestBody OrderRequest request) {
        Long id = getNewOrderId();
        SQLExecutor.addNewRequest(request, id);
        // TODO: send request in warehouse server. Returns order_id.
        return id;
    }

    @RequestMapping(value = "/cancellation/{order_id}", method = RequestMethod.PUT)
    public void cancelRequest(@PathVariable int order_id) {
        // TODO: send request in warehouse server.
    }
}
