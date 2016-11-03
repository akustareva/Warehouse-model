package warehouse.model.merchandiser.webserver;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import warehouse.model.entities.GetRequest;
import warehouse.model.entities.OrderRequest;
import warehouse.model.entities.User;
import warehouse.model.merchandiser.webserver.db.SQLExecutor;

@RestController
@RequestMapping("/mh")
public class Queries {

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public Integer userSignUp(@RequestBody User user) {
        return SQLExecutor.insert(user);
    }

    @RequestMapping(value = "/goods", method = RequestMethod.POST)
    public Integer CheckRequest(@RequestBody GetRequest request) {
        SQLExecutor.addNewRequest(request);
        // TODO: send request in warehouse server
        return 0;
    }

    @RequestMapping(value = "/order", method = RequestMethod.POST)
    public void Request(@RequestBody OrderRequest request) {
        SQLExecutor.addNewRequest(request);
        // TODO: send request in warehouse server
    }
}
