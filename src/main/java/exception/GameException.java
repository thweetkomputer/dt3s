package exception;

import lombok.Data;

@Data
public class GameException extends Exception {
    private String username;
    public GameException(String username) {
        super(username);
        this.username = username;
    }
}
