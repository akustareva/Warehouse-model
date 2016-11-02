package warehouse.model.webserver.entities;

public class GoodsDisplay {
    private long articul_id, warehouse_id, status_id, quantity;
    private Long user_id;

    public GoodsDisplay(long articul_id, long warehouse_id, long status_id, long quantity, Long user_id) {
        this.articul_id = articul_id;
        this.warehouse_id = warehouse_id;
        this.status_id = status_id;
        this.quantity = quantity;
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return String.format("Record[ articul_id=%d, warehouse_id=%d, status_id=%d, user_id=%d, quantity=%d, ]",
                articul_id, warehouse_id, status_id, user_id, quantity);
    }
}
