package warehouse.model.webserver.entities;

public class StatusList {
    private long articul_id;
    private String description;

    public StatusList(long articul_id, String description) {
        this.articul_id = articul_id;
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("Record[ status_id=%d, description=%s ]", articul_id, description);
    }
}
