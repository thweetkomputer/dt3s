// Chen Zhao 1427714
package server.rmi;

import client.rmi.GameCallBackInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * the login interface.
 */
public interface LoginInterface extends Remote {
    /**
     * login.
     *
     * @param username   the username.
     * @param gameClient the game client.
     * @throws RemoteException the remote exception.
     */
    void Login(String username, GameCallBackInterface gameClient) throws RemoteException;

    /**
     * logout
     *
     * @param username the username.
     * @throws RemoteException the remote exception.
     */
    void Logout(String username) throws RemoteException;
}
