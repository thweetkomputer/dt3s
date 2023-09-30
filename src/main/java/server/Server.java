// Chen Zhao 1427714
package server;

import client.rmi.GameCallBackInterface;
import common.Game;
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
    private final Lock lock = new ReentrantLock();
    /**
     * condition.
     */
    private final Condition condition = lock.newCondition();
    /**
     * random.
     */
    private final Random random = new Random();
    /**
     * thread pool.
     */
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            5,
            10,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100)
    );
    /**
     * login service.
     */
    private LoginInterface loginService;
    /**
     * game service.
     */
    private GameInterface gameService;


    public void start() {
        startRegistry();
        startServices();

        LOGGER.info("Server starting ...");
        startServer();

    }

    private void startServer() {
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
            playingPlayers.put(player1.getUsername(), player1);
            playingPlayers.put(player2.getUsername(), player2);
            lock.unlock();
            // start game
            executor.execute(() -> startGame(player1, player2));
        }
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
            loginService = new LoginImpl(players, playerList, freePlayers, playingPlayers, gameClients, lock);
            registerService(loginService, "login");
            gameService = new GameImpl(players, playerList, freePlayers, playingPlayers, gameClients, lock, condition);
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

    private void startGame(Player player1, Player player2) {
        LOGGER.info("Start game between " + player1.getUsername() + " and " + player2.getUsername() + ".");
        // start game
        // TODO: in case player1 or player2 is offline
        Game game;
        lock.lock();
        playingPlayers.put(player1.getUsername(), player1);
        playingPlayers.put(player2.getUsername(), player2);
        Player[] players = new Player[]{player1, player2};
        GameCallBackInterface client1 = gameClients.get(player1.getUsername());
        GameCallBackInterface client2 = gameClients.get(player2.getUsername());
        String[] chess;
        GameCallBackInterface[] clients = new GameCallBackInterface[]{client1, client2};
        // random between 0 and 1
        int randomInt = random.nextInt(2);
        if (randomInt == 0) {
            chess = new String[]{"X", "O"};
        } else {
            chess = new String[]{"O", "X"};
        }
        int turn = random.nextInt(2);
        LOGGER.info("Turn: " + turn);
        game = new Game(new char[][]{
                {' ', ' ', ' '},
                {' ', ' ', ' '},
                {' ', ' ', ' '}
        }, turn, -1, 20, players, new String[]{player1.toString(), player2.toString()},
                playerList, chess, lock, clients, new ReentrantLock());
        player1.setGame(game);
        player2.setGame(game);
        lock.unlock();
        try {
            client1.startGame(player2.getUsername(), System.currentTimeMillis(), game.getTurnLabel());
        } catch (RemoteException e) {
            LOGGER.info("Start game for " + player1.getUsername() + " failed: " + e.getMessage());
            lock.lock();
            freePlayers.put(player2.getUsername(), player2);
            playingPlayers.remove(player1.getUsername());
            playingPlayers.remove(player2.getUsername());
            lock.unlock();
            return;
        }
        try {
            client2.startGame(player1.getUsername(), System.currentTimeMillis(), game.getTurnLabel());
        } catch (RemoteException e) {
            LOGGER.info("Start game for " + player2.getUsername() + " failed: " + e.getMessage());
            lock.lock();
            freePlayers.put(player1.getUsername(), player1);
            playingPlayers.remove(player1.getUsername());
            playingPlayers.remove(player2.getUsername());
            lock.unlock();
            return;
        }
        try {
            game.start();
        } catch (GameException ge) {
            LOGGER.info("Game exception:" + ge.getMessage());
            String username = ge.getUsername();
            // TODO add another to freePlayers
        } catch (Exception e) {
            e.printStackTrace();
            // TODO
        }
    }
}
