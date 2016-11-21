package warehouse.model.webserver;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<Long> bookRequest(@RequestBody Request request) {
        try {
            SQLExecutor.addNewRequest(request);
        } catch (DataAccessException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(request.getId(), HttpStatus.OK);
    }

    @RequestMapping(value = "/payment/{order_id}", method = RequestMethod.PUT)
    public void paymentRequest(@PathVariable long order_id) {
        SQLExecutor.payOrder(order_id);
    }

    @RequestMapping(value = "/cancellation/{order_id}", method = RequestMethod.PUT)
    public void cancelRequest(@PathVariable long order_id) {
        SQLExecutor.cancelOrder(order_id);
    }
}
