package warehouse.model.webserver;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import warehouse.model.entities.Goods;
import warehouse.model.entities.Request;
import warehouse.model.webserver.db.SQLExecutor;

import java.util.List;

@RestController
@RequestMapping("/wh")
public class Queries {

    @RequestMapping(value = "/new_item", method = RequestMethod.POST)
    public void addGoods(@RequestBody Goods goods) {
        SQLExecutor.insert(goods);
    }

    @RequestMapping(value = "/goods/{good_id}", method = RequestMethod.GET)
    public ResponseEntity<Integer> checkRequest(@PathVariable int good_id) {
        Integer count = SQLExecutor.getGoodsCount(good_id);
        if (count == -1) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @RequestMapping(value = "/all_goods", method = RequestMethod.GET)
    public ResponseEntity<List<Goods>> showRequest() {
        List<Goods> goods = SQLExecutor.showAllGoods();
        if (goods == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(goods, HttpStatus.OK);
    }

    @RequestMapping(value = "/book", method = RequestMethod.POST)
    public ResponseEntity bookRequest(@RequestBody Request request) {
        HttpStatus status = SQLExecutor.addNewRequest(request);
        return new ResponseEntity(status);
    }

    @RequestMapping(value = "/payment/{order_id}", method = RequestMethod.PUT)
    public ResponseEntity paymentRequest(@PathVariable long order_id) {
        HttpStatus status = SQLExecutor.payOrder(order_id);
        return new ResponseEntity(status);
    }

    @RequestMapping(value = "/cancellation/{order_id}", method = RequestMethod.PUT)
    public ResponseEntity cancelRequest(@PathVariable long order_id) {
        HttpStatus status = SQLExecutor.cancelOrder(order_id);
        return new ResponseEntity(status);
    }
}
