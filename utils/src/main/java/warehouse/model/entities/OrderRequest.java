package warehouse.model.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderRequest extends Request {
    @JsonProperty("amount")
    private int amount;
    @JsonProperty("type")
    private RequestType type;

    @JsonCreator
    public OrderRequest(@JsonProperty("user") int userId, @JsonProperty("code") int uniqueCode,
                        @JsonProperty("amount")int amount, @JsonProperty("type") RequestType type)
    {
        this.userId = userId;
        this.uniqueCode = uniqueCode;
        this.amount = amount;
        this.type = type;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public RequestType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("OrderRequest [user = %d, product = %d, amount = %d, type = %s]",
                userId, uniqueCode, amount, type.toString());
    }

    public enum RequestType {
        BUY ("buy"),
        BOOK ("book");

        private String text;
        RequestType(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
