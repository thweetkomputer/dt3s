package server.rmi;

import client.rmi.GameCallBackInterface;
import common.Game;
import common.Player;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
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

    private ConcurrentHashMap<String, GameCallBackInterface> clients;

    private ConcurrentHashMap<String, Player> freePlayers;
    private ConcurrentHashMap<String, Player> playingPlayers;
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

    /**
     * make a move.
     *
     * @param username the username.
     * @param x        the x position.
     * @param y        the y position.
     */
    @Override
    public String makeMove(String username, int x, int y) {
        Player player = playingPlayers.get(username);
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
        try {
            game.move(x, y);
        } catch (RemoteException e) {
            e.printStackTrace();
            // TODO
        }
        return "OK";
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
        playingPlayers.put(player1.getUsername(), player1);
        playingPlayers.put(player2.getUsername(), player2);
        lock.unlock();

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
        }, 0, -1, players, chess, clients);
        player1.setGame(game);
        player2.setGame(game);

        try {
            client1.startGame(player1.getUsername(), System.currentTimeMillis(), game.getTurnLabel());
            client2.startGame(player2.getUsername(), System.currentTimeMillis(), game.getTurnLabel());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        while (!game.finished()) {
        }
    }
}
