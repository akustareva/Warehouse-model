package warehouse.model.webserver;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import warehouse.model.entities.GetRequest;
import warehouse.model.entities.OrderRequest;
import warehouse.model.entities.User;
import warehouse.model.webserver.db.SQLExecutor;

@RestController
public class Queries {

    @RequestMapping(value = "/warehouse/sign_up", method = RequestMethod.PUT)
    public void userSignUp(@RequestBody User user) {
        SQLExecutor.insert(user);
        // TODO: return unique id?
    }

    @RequestMapping(value = "/warehouse/get", method = RequestMethod.POST)
    public int CheckRequest(@RequestBody GetRequest request) {
        SQLExecutor.addNewRequest(request);
        // TODO: send request in warehouse server
        return 0;
    }

    @RequestMapping(value = "/warehouse/order", method = RequestMethod.POST)
    public void Request(@RequestBody OrderRequest request) {
        SQLExecutor.addNewRequest(request);
        // TODO: send request in warehouse server
    }
}
