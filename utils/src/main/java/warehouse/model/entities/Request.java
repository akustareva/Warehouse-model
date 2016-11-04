package warehouse.model.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Request {
    @JsonProperty("user")
    private int userId;
    @JsonProperty("code")
    private int uniqueCode;
    @JsonProperty("amount")
    private int amount;
    @JsonProperty("type")
    private RequestType type;

    @JsonCreator
    public Request(@JsonProperty("user") int userId, @JsonProperty("code") int uniqueCode,
                   @JsonProperty("amount")int amount, @JsonProperty("type") RequestType type)
    {
        this.userId = userId;
        this.uniqueCode = uniqueCode;
        this.amount = amount;
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public int getUniqueCode() {
        return uniqueCode;
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
        return String.format("Request [user = %d, product = %d, amount = %d, type = %s]",
                userId, uniqueCode, amount, type.toString());
    }

    public enum RequestType {
        BOOKED ("booked"),
        PAID("paid"),
        CANCELED("canceled");

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
