package client.rmi;

import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicLong;

/**
 * the game callback implementation.
 */
public class GameCallBackImpl extends UnicastRemoteObject implements GameCallBackInterface {

    private AtomicLong lastMessageTime = null;

    /**
     * the constructor.
     */
    public GameCallBackImpl() throws Exception {
        super();
    }

    /**
     * the constructor.
     */
    public GameCallBackImpl(AtomicLong lastMessageTime) throws Exception {
        super();
        this.lastMessageTime = lastMessageTime;
    }

    /**
     * start the game.
     *
     * @param opponent    the opponent.
     * @param messageTime the message time, used to stop the timer.
     */
    @Override
    public void startGame(String opponent, long messageTime) {
        System.out.println("start game with " + opponent);
        this.lastMessageTime.set(messageTime);
    }
}
