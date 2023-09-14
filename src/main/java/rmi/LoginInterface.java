package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * the login interface.
 */
public interface LoginInterface extends Remote {
    /**
     * login.
     * @param username the username.
     * @return the information, "OK" if success, others if failed.
     * @throws RemoteException the remote exception.
     */
    String Login(String username) throws RemoteException;
}
