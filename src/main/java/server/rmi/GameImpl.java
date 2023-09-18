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

    private final Random random = new Random();

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            5,
            10,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100)
    );

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
            condition.signalAll();
        } finally {
            lock.unlock();
        }
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
                // start game
                executor.execute(() -> StartGame(player1, player2));
            }
        }).start();
    }

    public void StartGame(Player player1, Player player2) {
        LOGGER.info("Start game between " + player1.getUsername() + " and " + player2.getUsername() + ".");
        // start game
        // TODO: in case player1 or player2 is offline
        lock.lock();
        try {
            playingPlayers.put(player1.getUsername(), player1);
            playingPlayers.put(player2.getUsername(), player2);

            Player[] players;
            String[] chess;
            GameCallBackInterface[] clients;
            // random between 0 and 1
            int randomInt = random.nextInt(2);
            if (randomInt == 0) {
                chess = new String[]{"X", "O"};
            } else {
                chess = new String[]{"O", "X"};
            }
            randomInt = random.nextInt(2);
            GameCallBackInterface client1 = this.clients.get(player1.getUsername());
            GameCallBackInterface client2 = this.clients.get(player2.getUsername());
            if (randomInt == 0) {
                players = new Player[]{player1, player2};
                clients = new GameCallBackInterface[]{client1, client2};
            } else {
                players = new Player[]{player2, player1};
                clients = new GameCallBackInterface[]{client2, client1};
            }
            Game game = new Game(new char[][]{
                    {' ', ' ', ' '},
                    {' ', ' ', ' '},
                    {' ', ' ', ' '}
            }, 0, -1, players, new String[]{player1.toString(), player2.toString()},
                    playerList, chess, lock, clients);
            player1.setGame(game);
            player2.setGame(game);

            client1.startGame(player1.getUsername(), System.currentTimeMillis(), game.getTurnLabel());
            client2.startGame(player2.getUsername(), System.currentTimeMillis(), game.getTurnLabel());
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        // TODO timer
    }
}
