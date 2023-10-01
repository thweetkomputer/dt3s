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
     * @param opponent the opponent.
     * @param turn     the turn.
     * @param label    the label.
     * @throws RemoteException the remote exception.
     */
    void startGame(String opponent, String turn, String label) throws RemoteException;

    /**
     * move.
     *
     * @param chess the chess.
     * @param x     the x coordinate.
     * @param y     the y coordinate.
     * @param turn  the turn.
     * @param label the label.
     * @throws RemoteException the remote exception.
     */
    void move(String chess, int x, int y, String turn, String label) throws RemoteException;

    /**
     * continue
     *
     * @param board the board.
     * @param turn  the turn.
     * @param label the label.
     * @throws RemoteException the remote exception.
     */
    void continueGame(char[][] board, String turn, String label) throws RemoteException;

    /**
     * heartbeat.
     *
     * @throws RemoteException the remote exception.
     */
    void heartbeat() throws RemoteException;

    /**
     * set label.
     *
     * @param label      the label.
     * @param resetTimer whether reset timer.
     * @throws RemoteException the remote exception.
     */
    void setLabel(String label, boolean resetTimer) throws RemoteException;

    /**
     * send message.
     *
     * @param message the message.
     * @throws RemoteException the remote exception.
     */
    void send(String message) throws RemoteException;

    /**
     * end game.
     *
     * @param chess the chess.
     * @param x     the x coordinate.
     * @param y     the y coordinate.
     * @param turn  the turn.
     * @param label the label.
     * @throws RemoteException the remote exception.
     */
    void endGame(String chess, int x, int y, String turn, String label) throws RemoteException;

    /**
     * set timer.
     *
     * @param timer           the timer.
     * @param lastMessageTime the last message time.
     * @return true if success, false if failed.
     * @throws RemoteException the remote exception.
     */
    boolean setTimer(int timer, Long lastMessageTime) throws RemoteException;
}
