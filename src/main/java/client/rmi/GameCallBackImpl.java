package client.rmi;

import javax.swing.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * the game callback implementation.
 */
public class GameCallBackImpl extends UnicastRemoteObject implements GameCallBackInterface {

    private AtomicLong lastMessageTime = null;

    private JLabel turnLabel = null;
    private JButton[] board = null;

    private ReadWriteLock mu = null;


    /**
     * the constructor.
     */
    public GameCallBackImpl() throws Exception {
        super();
    }

    /**
     * the constructor.
     */
    public GameCallBackImpl(ReadWriteLock mu, AtomicLong lastMessageTime, JButton[] board, JLabel turnLabel) throws Exception {
        super();
        this.mu = mu;
        this.lastMessageTime = lastMessageTime;
        this.turnLabel = turnLabel;
        this.board = board;
    }

    /**
     * start the game.
     *
     * @param opponent    the opponent.
     * @param messageTime the message time, used to stop the timer.
     * @param turn        the turn.
     */
    @Override
    public void startGame(String opponent, long messageTime, String turn) {
        System.out.println("start game with " + opponent);
        mu.writeLock().lock();
        this.lastMessageTime.set(messageTime);
        this.turnLabel.setText(turn);
        mu.writeLock().unlock();
    }

    /**
     * move.
     *
     * @param chess the chess.
     * @param x     the x coordinate.
     * @param y     the y coordinate.
     * @param turn  the turn.
     */
    @Override
    public void move(String chess, int x, int y, String turn) {
        mu.writeLock().lock();
        this.board[x * 3 + y].setText(chess);
        this.turnLabel.setText(turn);
        mu.writeLock().unlock();
    }
}
