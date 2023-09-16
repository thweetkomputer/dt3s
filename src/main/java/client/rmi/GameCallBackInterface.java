package client.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * the game callback interface.
 */
public interface GameCallBackInterface extends Remote {
    /**
     * start the game.
     * @param opponent the opponent.
     * @param messageTime the message time, used to stop the timer.
     * @throws RemoteException the remote exception.
     */
    void startGame(String opponent, long messageTime) throws RemoteException;
}
