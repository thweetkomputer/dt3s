// Chen Zhao 1427714
package common;

import client.rmi.GameCallBackInterface;
import lombok.Data;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * the player class, contains the information of a player.
 */
@Data
public class Player implements Comparable<Player> {
    String username;
    int rank;
    int score;
    long loginTime;

    Game game;
    Lock mu = new ReentrantLock();
    GameCallBackInterface client;

    /**
     * the constructor.
     *
     * @param username  the username.
     * @param rank      the rank.
     * @param loginTime the login time.
     * @param client    the client.
     */
    public Player(String username, int rank, long loginTime, GameCallBackInterface client) {
        this.username = username;
        this.score = 0;
        this.rank = rank;
        this.loginTime = loginTime;
        this.client = client;
    }

    @Override
    public String toString() {
        if (username.length() > 10) {
            return "Rank#" + rank + " " + username.substring(0, 7) + "...";
        } else {
            return "Rank#" + rank + " " + username;
        }
    }

    /**
     * win.
     */
    public void win() {
        score += 5;
    }

    /**
     * lose.
     */
    public void lose() {
        score -= 5;
    }

    /**
     * draw.
     */
    public void draw() {
        score += 2;
    }

    @Override
    public int compareTo(Player o) {
        if (score != o.score) {
            return o.score - score;
        } else {
            return (int) (loginTime - o.loginTime);
        }
    }

    public GameCallBackInterface getClient() {
        mu.lock();
        GameCallBackInterface ret = client;
        mu.unlock();
        return ret;
    }

    public void setClient(GameCallBackInterface client) {
        mu.lock();
        this.client = client;
        mu.unlock();
    }
}
