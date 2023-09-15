package server.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * the game implementation.
 */
public class GameImpl extends UnicastRemoteObject implements GameInterface {

    /**
     * the constructor.
     */
    public GameImpl() throws RemoteException {
        super();
    }
}
