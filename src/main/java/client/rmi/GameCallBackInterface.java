package client.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * the game callback interface.
 */
public interface GameCallBackInterface extends Remote {
    /**
     * start the game.
     *
     * @param opponent    the opponent.
     * @param messageTime the message time, used to stop the timer.
     * @throws RemoteException the remote exception.
     */
    void startGame(String opponent, long messageTime, String turn) throws RemoteException;

    /**
     * move.
     *
     * @param chess the chess.
     * @param x     the x coordinate.
     * @param y     the y coordinate.
     * @param turn  the turn.
     * @throws RemoteException the remote exception.
     */
    void move(String chess, int x, int y, String turn) throws RemoteException;
}
