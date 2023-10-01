// Chen Zhao 1427714
package server;

import client.rmi.GameCallBackInterface;
import common.Game;
import common.MyReentrantLock;
import common.Player;
import exception.GameException;
import server.rmi.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * the server class.
 */
public class Server {
    private static final Logger LOGGER = Logger.getLogger(GameImpl.class.getName());

    private String ip;
    private int port;

    public Server(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * all players.
     */
    private final HashMap<String, Player> players = new HashMap<>();
    /**
     * player list.
     */
    private final TreeSet<Player> playerList = new TreeSet<>();
    /**
     * free players.
     */
    private final HashMap<String, Player> freePlayers = new HashMap<>();
    /**
     * playing players.
     */
    private final HashMap<String, Player> playingPlayers = new HashMap<>();
    /**
     * clients.
     */
    private final HashMap<String, GameCallBackInterface> gameClients = new HashMap<>();
    /**
     * lock.
     */
    private final Lock lock = new MyReentrantLock();



    public void start() {
        startRegistry();
        startServices();

        LOGGER.info("Server starting ...");
    }


    private void startRegistry() {
        try {
            LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            LOGGER.info("Server registry failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private void startServices() {
        try {
            LoginInterface loginService = new LoginImpl(players, playerList, freePlayers, playingPlayers, gameClients, lock);
            registerService(loginService, "login");
            GameInterface gameService = new GameImpl(players, playerList, freePlayers, playingPlayers, gameClients, lock);
            registerService(gameService, "game");
        } catch (RemoteException | MalformedURLException e) {
            LOGGER.info("Server register service failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private void registerService(Remote service, String serviceName) throws RemoteException, MalformedURLException {
        String url = String.format("rmi://%s:%d/%s", ip, port, serviceName);
        Naming.rebind(url, service);
        System.out.println("Service " + serviceName + " bound to " + url);
    }


}
