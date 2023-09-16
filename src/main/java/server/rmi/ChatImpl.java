package server.rmi;

import client.rmi.ChatCallBackInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * the chat implementation.
 */
public class ChatImpl extends UnicastRemoteObject implements ChatInterface {

    private ConcurrentHashMap<String, ChatCallBackInterface> clients;

    /**
     * the constructor.
     */
    public ChatImpl() throws RemoteException {
        super();
    }

    /**
     * the constructor.
     */
    public ChatImpl(ConcurrentHashMap<String, ChatCallBackInterface> clients) throws RemoteException {
        super();
        this.clients = clients;
    }

    /**
     * send message.
     *
     * @param username the username.
     * @param message  the message.
     * @throws RemoteException the remote exception.
     */
    @Override
    public void sendMessage(String username, String message) throws RemoteException {
        System.out.println(username + ": " + message);
    }
}
