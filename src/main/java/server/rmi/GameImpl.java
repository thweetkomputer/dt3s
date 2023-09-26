// Chen Zhao 1427714
package server.rmi;

import client.rmi.GameCallBackInterface;
import common.Game;
import common.Player;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

/**
 * the game implementation.
 */
public class GameImpl extends UnicastRemoteObject implements GameInterface {
    private static final Logger LOGGER = Logger.getLogger(GameImpl.class.getName());

    private HashMap<String, GameCallBackInterface> clients;

    private HashMap<String, Player> allPlayers;
    private TreeSet<Player> playerList;
    private HashMap<String, Player> freePlayers;
    private HashMap<String, Player> playingPlayers;
    private Lock lock;
    private Condition condition;




    /**
     * the constructor.
     */
    public GameImpl() throws RemoteException {
        super();
    }

    /**
     * the constructor.
     */
    public GameImpl(HashMap<String, Player> players,
                    TreeSet<Player> playerList,
                    HashMap<String, Player> freePlayers,
                    HashMap<String, Player> playingPlayers,
                    HashMap<String, GameCallBackInterface> clients,
                    Lock lock,
                    Condition condition) throws RemoteException {
        super();
        this.allPlayers = players;
        this.playerList = playerList;
        this.freePlayers = freePlayers;
        this.playingPlayers = playingPlayers;
        this.clients = clients;
        this.lock = lock;
        this.condition = condition;
    }

    /**
     * make a move.
     *
     * @param username the username.
     * @param x        the x position.
     * @param y        the y position.
     */
    @Override
    public String makeMove(String username, int x, int y) {
        try {
            lock.lock();
            Player player = playingPlayers.get(username);
            lock.unlock();
            if (player == null) {
                return "You are not in a game.";
            }
            Game game = player.getGame();
            if (game == null) {
                return "You are not in a game.";
            }
            if (!game.isMyTurn(username)) {
                return "It's not your turn.";
            }
            game.move(x, y);
        } catch (RemoteException e) {
            e.printStackTrace();
            // TODO
        }
        return "OK";
    }

    /**
     * send message.
     *
     * @param username the username.
     * @param message  the message.
     */
    @Override
    public void sendMessage(String username, String message) {
        lock.lock();
        try {
            Player player = playingPlayers.get(username);
            if (player == null) {
                return;
            }
            Game game = player.getGame();
            if (game == null) {
                return;
            }
            message = player + ": " + message;
            game.getClients()[0].send(message);
            game.getClients()[1].send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            // TODO
        } finally {
            lock.unlock();
        }
    }

    /**
     * find a game.
     *
     * @param username the username.
     */
    @Override
    public void findGame(String username) {
        lock.lock();
        try {
            Player player = allPlayers.get(username);
            if (player == null) {
                return;
            }
            freePlayers.put(username, player);
            LOGGER.info("Player " + username + " is looking for a game.");
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }


}
