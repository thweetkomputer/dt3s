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
 * the game implementation.
 */
public class GameImpl extends UnicastRemoteObject implements GameInterface {
    private static final Logger LOGGER = Logger.getLogger(GameImpl.class.getName());

    private ConcurrentHashMap<String, GameCallBackInterface> clients;

    private ConcurrentHashMap<String, Player> freePlayers;
    private ConcurrentHashMap<String, Player> playingPlayers;
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
    public GameImpl(ConcurrentHashMap<String, Player> freePlayers,
                    ConcurrentHashMap<String, Player> playingPlayers,
                    ConcurrentHashMap<String, GameCallBackInterface> clients,
                    Lock lock,
                    Condition condition) throws RemoteException {
        super();
        this.freePlayers = freePlayers;
        this.playingPlayers = playingPlayers;
        this.clients = clients;
        this.lock = lock;
        this.condition = condition;
    }

    public void Start() {
        new Thread(() -> {
            while (true) {
                LOGGER.info("Waiting for players...");
                lock.lock();
                while (freePlayers.size() < 2) {
                    try {
                        LOGGER.info("Not enough players, waiting...");
                        condition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // get two players
                Player player1 = freePlayers.values().iterator().next();
                freePlayers.remove(player1.getUsername());
                Player player2 = freePlayers.values().iterator().next();
                freePlayers.remove(player2.getUsername());
                lock.unlock();
                LOGGER.info("Start game between " + player1.getUsername() + " and " + player2.getUsername() + ".");
                // start game
                // TODO: in case player1 or player2 is offline
                try {
                    clients.get(player1.getUsername()).startGame(player2.getUsername(), System.currentTimeMillis());
                    clients.get(player2.getUsername()).startGame(player1.getUsername(), System.currentTimeMillis());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                playingPlayers.put(player1.getUsername(), player1);
                playingPlayers.put(player2.getUsername(), player2);
            }
        }).start();
    }
}
