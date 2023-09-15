package client.rmi;

import java.rmi.server.UnicastRemoteObject;

/**
 * the chat callback implementation.
 */
public class ChatCallBackImpl extends UnicastRemoteObject implements ChatCallBackInterface {

    /**
     * the constructor.
     */
    public ChatCallBackImpl() throws Exception {
        super();
    }

    @Override
    public void sendMessage(String username, String message) {
        System.out.println(username + ": " + message);
    }
}
