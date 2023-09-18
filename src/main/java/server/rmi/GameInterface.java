package server.rmi;

import client.rmi.GameCallBackInterface;

import java.rmi.Remote;

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
    String makeMove(String username, int x, int y) throws Exception;


}
