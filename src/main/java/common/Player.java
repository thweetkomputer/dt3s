package common;

import lombok.Data;

/**
 * the player class, contains the information of a player.
 */
@Data
public class Player {
    String username;
    int rank;
    int score;
    long loginTime;

    /**
     * the constructor.
     *
     * @param username  the username.
     * @param rank      the rank.
     * @param loginTime the login time.
     */
    public Player(String username, int rank, long loginTime) {
        this.username = username;
        this.score = 0;
        this.rank = rank;
        this.loginTime = loginTime;
    }
}
