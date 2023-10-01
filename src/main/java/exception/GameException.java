package exception;

import lombok.Data;

@Data
public class GameException extends Exception {
    private String[] username;
    public GameException(String username) {
        super(username);
        this.username = new String[1];
        this.username[0] = username;
    }
    public GameException(String[] username) {
        super(username[0]);
        this.username = username;
    }
}
