// Chen Zhao 1427714
package client.rmi;

import exception.GameException;

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

    /**
     * set label.
     *
     * @param label the label.
     * @throws RemoteException the remote exception.
     */
    void setLabel(String label) throws RemoteException;

    /**
     * send message.
     *
     * @param message the message.
     * @throws RemoteException the remote exception.
     */
    void send(String message) throws RemoteException;

    /**
     * ask for a rematch.
     *
     * @throws RemoteException the remote exception.
     */
    void ask() throws RemoteException;

    /**
     * set timer.
     *
     * @param timer the timer.
     * @throws RemoteException the remote exception.
     */
    void setTimer(int timer) throws RemoteException;
}
