package server;

import common.Player;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

/**
 * the login implementation.
 */
public class LoginImpl extends UnicastRemoteObject implements rmi.LoginInterface {
    private final ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();

    /**
     * the constructor.
     *
     * @throws RemoteException the remote exception.
     */
    public LoginImpl() throws RemoteException {
        super();
    }

    /**
     * login.
     *
     * @param username the username.
     * @return the information, "OK" if success, others if failed.
     * @throws RemoteException the remote exception.
     */
    @Override
    public String Login(String username) throws RemoteException {
        if (players.containsKey(username)) {
            return "Username already exists.";
        }
        Player player = new Player(username, players.size() + 1);
        players.put(username, player);
        return "OK";
    }

    /**
     * logout.
     *
     * @param username the username.
     * @throws RemoteException the remote exception.
     */
    @Override
    public void Logout(String username) throws RemoteException {
        players.remove(username);
    }
}
