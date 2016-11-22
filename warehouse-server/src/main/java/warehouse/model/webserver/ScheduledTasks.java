package warehouse.model.webserver;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import warehouse.model.webserver.db.SQLExecutor;

@Component
public class ScheduledTasks {

    @Scheduled(fixedRate = 7776000000L)        // delete booked requests which was sent 3 and more months ago and canceled requests
    public void deleteOldRequests() {
        SQLExecutor.deleteOldRequests();
    }
}
