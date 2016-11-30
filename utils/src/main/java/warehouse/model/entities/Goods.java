package warehouse.model.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Goods {
    @JsonProperty("code")
    private int code;
    @JsonProperty("count")
    private int quantity;
    @JsonProperty("name")
    private String name;

    @JsonCreator
    public Goods(@JsonProperty("code") int code, @JsonProperty("count") int quantity, @JsonProperty("name") String name) {
        this.code = code;
        this.quantity = quantity;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getName() {
        return name;
    }

    public String toQuery() {
        return "(" + code + ", " + quantity + ", '" + name + "')";
    }

    @Override
    public String toString() {
        return String.format("Goods [id = %d, quantity = %d, name = %s]", code, quantity, name);
    }
}
