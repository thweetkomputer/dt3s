package server.rmi;

import client.rmi.GameCallBackInterface;
import common.Player;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;
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

    private HashMap<String, Player> players;
    private TreeSet<Player> playerList;
    private HashMap<String, Player> freePlayers;
    private HashMap<String, Player> playingPlayers;
    private HashMap<String, GameCallBackInterface> gameClients;
    private Lock lock;

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
    public LoginImpl(HashMap<String, Player> players,
                     TreeSet<Player> playerList,
                     HashMap<String, Player> freePlayers,
                     HashMap<String, Player> playingPlayers,
                     HashMap<String, GameCallBackInterface> gameClients,
                     Lock lock,
                     Condition condition) throws RemoteException {
        super();
        this.players = players;
        this.playerList = playerList;
        this.freePlayers = freePlayers;
        this.playingPlayers = playingPlayers;
        this.gameClients = gameClients;
        this.lock = lock;
    }

    /**
     * login.
     *
     * @param username   the username.
     * @param gameClient the game client.
     * @throws RemoteException the remote exception.
     */
    @Override
    public void Login(String username,
                      GameCallBackInterface gameClient) throws RemoteException {
        lock.lock();
        if (players.containsKey(username)) {
            Player player = players.get(username);
            gameClients.put(username, gameClient);
            if (playingPlayers.containsKey(username) || freePlayers.containsKey(username)) {
                lock.unlock();
                return;
            }
            freePlayers.put(username, player);
            lock.unlock();
            return;
        }
        Player player = new Player(username, players.size() + 1, System.currentTimeMillis());
        players.put(username, player);
        playerList.add(player);
        gameClients.put(username, gameClient);
        LOGGER.info("Player " + username + " login.");
        lock.unlock();
    }

    /**
     * logout.
     *
     * @param username the username.
     * @throws RemoteException the remote exception.
     */
    @Override
    public void Logout(String username) throws RemoteException {
        lock.lock();
        freePlayers.remove(username);
        playingPlayers.remove(username);
        lock.unlock();
        LOGGER.info("Player " + username + " logout.");
    }
}
