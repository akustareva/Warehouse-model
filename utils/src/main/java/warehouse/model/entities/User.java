package warehouse.model.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    @JsonProperty("login")
    private String login;
    @JsonProperty("password")
    private String password;

    @JsonCreator
    public User(@JsonProperty("login") String login, @JsonProperty("password") String password) {
        this.login = login;
        this.password = password;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return String.format("User [login = %s, password = %s]", login, password);
    }
}
