package warehouse.model.webserver.entities;

public class Warehouse {
    private long articul_id;
    private String name, address, telephone;

    public Warehouse(long articul_id, String name, String address, String telephone) {
        this.articul_id = articul_id;
        this.name = name;
        this.address = address;
        this.telephone = telephone;
    }

    @Override
    public String toString() {
        return String.format("Record[ articul_id=%d, name=%s, address=%s, telephone=%s ]",
                articul_id, name, address, telephone);
    }
}
