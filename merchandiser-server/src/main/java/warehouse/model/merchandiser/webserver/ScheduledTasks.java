package warehouse.model.merchandiser.webserver;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import warehouse.model.entities.Request;
import warehouse.model.merchandiser.webserver.db.SQLExecutor;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

@Component
public class ScheduledTasks {
    private static ResourceBundle bundle = ResourceBundle.getBundle("mh", Locale.US);
    private String whServerAddress = bundle.getString("wh.server.default.address");
    private RestTemplate restTemplate = new RestTemplate();
    private static String DONE_STATUS = "done";

    @Scheduled(fixedDelay = 5000)
    public void tryToUpdateInProgressRequests() {
        List<Request> requests = SQLExecutor.getAllInProgressRequests();
        if (requests == null || requests.size() == 0) {
            return;
        }
        requests.removeIf(Objects::isNull);
        for (Request request : requests) {
            try {
                if (request.getType() == Request.RequestType.BOOKED) {
                    ResponseEntity<Long> id = restTemplate.postForEntity(whServerAddress + "/book", request, Long.class);
                    if (id != null && id.getStatusCode() == HttpStatus.OK) {
                        SQLExecutor.updateOrderStatus(request.getId(), DONE_STATUS);
                    }
                } else if (request.getType() == Request.RequestType.PAID) {
                    restTemplate.put(whServerAddress + "/payment/" + request.getId(), null);
                    SQLExecutor.updateOrderStatus(request.getId(), DONE_STATUS);
                }
            } catch (RestClientException e) {
                SQLExecutor.incrementAttemptsCount(request.getId());
            } catch (DataAccessException ignored) {}
        }
    }

    @Scheduled(fixedRate = 7776000000L)        // delete booked requests which was sent 3 and more months ago and canceled requests
    public void deleteOldRequests() {
        SQLExecutor.deleteOldRequests();
    }

    @Scheduled(fixedRate = 3600000)
    public void updateGoods() {
        try {
            ResponseEntity<JsonNode> goods = restTemplate.getForEntity(whServerAddress + "/all_goods", JsonNode.class);
            SQLExecutor.updateGoodsTable(goods.getBody());
        } catch (RestClientException ignored) {}
    }
}
