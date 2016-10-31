package warehouse.model.webserver;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import warehouse.model.entities.User;
import warehouse.model.webserver.db.SQLExecutor;

@RestController
public class Queries {

    // TODO: These are examples of operations. They will be changed.

    @RequestMapping(value = "/warehouse/sign_up", method = RequestMethod.PUT)
    public void userSignUp(@RequestBody User user) {
        SQLExecutor.insert(user.getLogin(), user.getPassword());
    }

    @RequestMapping(value = "/warehouse/get", method = RequestMethod.POST)
    public String CheckRequest(Integer uniqueCode) {
        return "You sent request for check the number of available product with code " + uniqueCode;
    }

    @RequestMapping("/warehouse/buy")
    public String PurchaseRequest(int uniqueCode, int count) {
        return "You sent request for buy product with code " + uniqueCode
                + " in an amount of " + count + " items";
    }
}
