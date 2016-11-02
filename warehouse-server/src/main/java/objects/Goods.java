package objects;

public class Goods {
    private long articul_id;
    private String merchandise_name, description;

    public Goods(long articul_id, String merchandise_name, String description) {
        this.articul_id = articul_id;
        this.merchandise_name = merchandise_name;
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("Record[ articul_id=%d, merchandise_name=%s, description=%s ]",
                articul_id, merchandise_name, description);
    }
}
