package warehouse.model.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GetRequest extends Request{

    @JsonCreator
    public GetRequest(@JsonProperty("user") int userId, @JsonProperty("code") int uniqueCode)
    {
        this.userId = userId;
        this.uniqueCode = uniqueCode;
    }

    @Override
    public String toString() {
        return String.format("GetRequest [user = %d, product = %d]", userId, uniqueCode);
    }
}
