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
     * @param username the username.
     * @return the information, "OK" if success, others if failed.
     * @throws RemoteException the remote exception.
     */
    String Login(String username, GameCallBackInterface gameClient) throws RemoteException;

    /**
     * logout
     *
     * @param username the username.
     * @throws RemoteException the remote exception.
     */
    void Logout(String username) throws RemoteException;
}
