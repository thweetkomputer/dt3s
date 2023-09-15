package server.rmi;

import client.rmi.ChatCallBackInterface;
import server.rmi.ChatInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * the chat implementation.
 */
public class ChatImpl extends UnicastRemoteObject implements ChatInterface {

    /**
     * the constructor.
     */
    public ChatImpl() throws RemoteException {
        super();
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

    /**
     * register.
     *
     * @param client the client.
     * @throws RemoteException the remote exception.
     */
    @Override
    public void register(ChatCallBackInterface client) throws RemoteException {
        System.out.println("register " + client);
        client.sendMessage("Server", "Welcome to the chat room!");
    }

    /**
     * unregister.
     *
     * @param client the client.
     * @throws RemoteException the remote exception.
     */
    @Override
    public void unregister(ChatCallBackInterface client) throws RemoteException {
        System.out.println("unregister " + client);
    }
}
