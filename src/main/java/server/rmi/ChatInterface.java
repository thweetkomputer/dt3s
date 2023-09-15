package server.rmi;


import client.rmi.ChatCallBackInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * the chat interface.
 */
public interface ChatInterface extends Remote {
    /**
     * send message.
     *
     * @param username the username.
     * @param message  the message.
     * @throws RemoteException the remote exception.
     */
    void sendMessage(String username, String message) throws RemoteException;

    void register(ChatCallBackInterface client) throws RemoteException;

    void unregister(ChatCallBackInterface client) throws RemoteException;
}
