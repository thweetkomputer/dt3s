// Chen Zhao 1427714
package server.rmi;

import client.rmi.GameCallBackInterface;
import exception.GameException;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * the game interface.
 */
public interface GameInterface extends Remote {
    /**
     * make a move.
     *
     * @param username the username.
     * @param x        the x position.
     * @param y        the y position.
     * @return the information, "OK" if success, others if failed.
     */
    String makeMove(String username, int x, int y) throws RemoteException, GameException;

    /**
     * send message.
     *
     * @param username the username.
     * @param message  the message.
     */
    void sendMessage(String username, String message) throws RemoteException, GameException;

    /**
     * find a game.
     *
     * @param username the username.
     * @param client   the client.
     */
    void findGame(String username, GameCallBackInterface client) throws RemoteException;

    /**
     * quit a game.
     *
     * @param username the username.
     * @throws RemoteException the remote exception.
     */
    void quit(String username) throws RemoteException;

    /**
     * heartbeat.
     */
    void heartbeat() throws RemoteException;
}
