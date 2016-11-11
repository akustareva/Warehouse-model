package warehouse.model.webserver;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import warehouse.model.entities.Goods;
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
    public Integer checkRequest(@PathVariable int good_id) {
        return SQLExecutor.getGoodsCount(good_id);
    }

    @RequestMapping(value = "/all_goods", method = RequestMethod.GET)
    public List<Goods> showRequest() {
        return SQLExecutor.showAllGoods();
    }
}
