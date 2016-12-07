package warehouse.model.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("goods")
    private String goodsName;
    @JsonProperty("quantity")
    private int quantity;
    @JsonProperty("type")
    private Request.RequestType type;
    @JsonProperty("status")
    private Request.RequestStatus status;

    @JsonCreator
    public Order(@JsonProperty("id") Long id, @JsonProperty("goods") String goodsName, @JsonProperty("amount")int quantity,
                 @JsonProperty("type") Request.RequestType type, @JsonProperty("status") Request.RequestStatus status)
    {
        this.id = id;
        this.goodsName = goodsName;
        this.quantity = quantity;
        this.type = type;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public int getQuantity() {
        return quantity;
    }

    public Request.RequestType getType() {
        return type;
    }

    public Request.RequestStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return String.format("Order [id = %d, goods = %s, quantity = %d, type = %s, status = %s]", id, goodsName,
                quantity, type.toString(), status.toString());
    }
}
