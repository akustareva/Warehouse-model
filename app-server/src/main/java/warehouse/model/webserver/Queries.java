package warehouse.model.webserver;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Queries {

    // TODO: These are examples of operations. They will be changed.

    @RequestMapping("/warehouse/buy")
    public String PurchaseRequest(int uniqueCode, int count) {
        return "You sent request for buy product with code " + uniqueCode
                + " in an amount of " + count + " items";
    }

    @RequestMapping("/warehouse/reservation")
    public String ReservationRequest(int uniqueCode, int count) {
        return "You sent request for reservation product with code " + uniqueCode
                + " in an amount of " + count + " items";
    }
}
