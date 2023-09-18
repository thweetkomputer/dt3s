package server.rmi;

import client.rmi.GameCallBackInterface;
import common.Player;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

/**
 * the login implementation.
 */
public class LoginImpl extends UnicastRemoteObject implements LoginInterface {
    /**
     * logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LoginImpl.class.getName());

    private ConcurrentHashMap<String, Player> players;
    private ConcurrentHashMap<String, Player> freePlayers;
    private ConcurrentHashMap<String, Player> playingPlayers;
    private ConcurrentHashMap<String, GameCallBackInterface> gameClients;
    private Lock lock;
    private Condition condition;

    /**
     * the constructor.
     *
     * @throws RemoteException the remote exception.
     */
    public LoginImpl() throws RemoteException {
        super();
    }

    /**
     * the constructor.
     */
    public LoginImpl(ConcurrentHashMap<String, Player> players,
                     ConcurrentHashMap<String, Player> freePlayers,
                     ConcurrentHashMap<String, Player> playingPlayers,
                     ConcurrentHashMap<String, GameCallBackInterface> gameClients,
                     Lock lock,
                     Condition condition) throws RemoteException {
        super();
        this.players = players;
        this.freePlayers = freePlayers;
        this.playingPlayers = playingPlayers;
        this.gameClients = gameClients;
        this.lock = lock;
        this.condition = condition;
    }

    /**
     * login.
     *
     * @param username the username.
     * @return the information, "OK" if success, others if failed.
     * @throws RemoteException the remote exception.
     */
    @Override
    public String Login(String username,
                        GameCallBackInterface gameClient) throws RemoteException {
        lock.lock();
        if (players.containsKey(username)) {
            lock.unlock();
            return "Username already exists.";
        }
        Player player = new Player(username, players.size() + 1, System.currentTimeMillis());
        players.put(username, player);
        freePlayers.put(username, player);
        gameClients.put(username, gameClient);
        LOGGER.info("Player " + username + " login.");
        LOGGER.info("Current free players: " + freePlayers.size() + ".");
        if (freePlayers.size() >= 2) {
            condition.signal();
        }
        lock.unlock();
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
        freePlayers.remove(username);
        playingPlayers.remove(username);
        LOGGER.info("Player " + username + " logout.");
    }
}
