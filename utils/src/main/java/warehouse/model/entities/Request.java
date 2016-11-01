package warehouse.model.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Request {
    @JsonProperty("user")
    protected int userId;
    @JsonProperty("code")
    protected int uniqueCode;

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUniqueCode(int uniqueCode) {
        this.uniqueCode = uniqueCode;
    }

    public int getUniqueCode() {
        return uniqueCode;
    }
}
