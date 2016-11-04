package warehouse.model.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Goods {
    @JsonProperty("code")
    private int code;
    @JsonProperty("count")
    private int quantity;

    @JsonCreator
    public Goods(@JsonProperty("code") int code, @JsonProperty("count") int quantity) {
        this.code = code;
        this.quantity = quantity;
    }

    public int getCode() {
        return code;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return String.format("Goods [id = %d, quantity = %s]", code, quantity);
    }
}
