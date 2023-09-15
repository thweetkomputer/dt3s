package client.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * the chat callback interface.
 */
public interface ChatCallBackInterface extends Remote {
    /**
     * send message.
     *
     * @param username the username.
     * @param message  the message.
     */
    void sendMessage(String username, String message) throws RemoteException;
}
