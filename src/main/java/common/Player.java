package common;

/**
 * the player class, contains the information of a player.
 */
public class Player {
    String username;
    int rank;
    int score;

    /**
     * the constructor.
     * @param username the username.
     */
    public Player(String username, int rank) {
        this.username = username;
        this.score = 0;
        this.rank = rank;
    }
}
