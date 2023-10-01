// Chen Zhao 1427714
package server.rmi;

import client.rmi.GameCallBackInterface;
import common.Game;
import common.Player;
import exception.GameException;

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
import java.util.concurrent.locks.ReentrantLock;
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
    /**
     * random.
     */
    private final Random random = new Random();
    private Lock lock;
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
                    Lock lock) throws RemoteException {
        super();
        this.allPlayers = players;
        this.playerList = playerList;
        this.freePlayers = freePlayers;
        this.playingPlayers = playingPlayers;
        this.clients = clients;
        this.lock = lock;
    }

    /**
     * make a move.
     *
     * @param username the username.
     * @param x        the x position.
     * @param y        the y position.
     * @return the information, "OK" if success, others if failed.
     * @throws RemoteException the remote exception.
     */
    @Override
    public String makeMove(String username, int x, int y) throws RemoteException, GameException {
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
        if (!game.isValidMove(x, y)) {
            return "Invalid move.";
        }
        game.move(x, y);
        return "OK";
    }

    /**
     * send message.
     *
     * @param username the username.
     * @param message  the message.
     * @throws RemoteException the remote exception.
     * @throws GameException   the game exception.
     */
    @Override
    public void sendMessage(String username, String message) throws RemoteException, GameException {
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
            // TODO collect fail clients
            try {
                game.getClients()[0].send(message);
            } catch (Exception e) {
                throw new GameException(game.getPlayers()[0].getUsername());
            }
            try {
                game.getClients()[1].send(message);
            } catch (Exception e) {
                throw new GameException(game.getPlayers()[1].getUsername());
            }
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
    public void findGame(String username) throws RemoteException {
        lock.lock();
        try {
            Player player = allPlayers.get(username);
            if (player == null) {
                return;
            }
            if (playingPlayers.containsKey(username) && playingPlayers.get(username).getGame() != null) {
                // continue game
                GameCallBackInterface client = clients.get(username);
                LOGGER.info("Player " + username + " is continuing a game.");
//                client.continueGame(); // TODO
            } else {
                freePlayers.put(username, player);
                LOGGER.info("Player " + username + " is looking for a game.");
                if (freePlayers.size() > 1) {
                    Player player1 = freePlayers.values().iterator().next();
                    freePlayers.remove(player1.getUsername());
                    Player player2 = freePlayers.values().iterator().next();
                    freePlayers.remove(player2.getUsername());
                    playingPlayers.put(player1.getUsername(), player1);
                    playingPlayers.put(player2.getUsername(), player2);
                    // start game
                    executor.execute(() -> startGame(player1, player2));
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void startGame(Player player1, Player player2) {
        // start game
        // TODO: in case player1 or player2 is offline
        Game game;
        lock.lock();
        LOGGER.info("Start game between " + player1.getUsername() + " and " + player2.getUsername() + ".");
        playingPlayers.put(player1.getUsername(), player1);
        playingPlayers.put(player2.getUsername(), player2);
        Player[] players = new Player[]{player1, player2};
        GameCallBackInterface client1 = clients.get(player1.getUsername());
        GameCallBackInterface client2 = clients.get(player2.getUsername());
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
                playerList, chess, lock, clients, new ReentrantLock(), playingPlayers);
        player1.setGame(game);
        player2.setGame(game);
        lock.unlock();
        try {
            client1.startGame(player2.getUsername(), players[turn].getUsername(), game.getTurnLabel());
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
            client2.startGame(player1.getUsername(), players[turn].getUsername(), game.getTurnLabel());
        } catch (RemoteException e) {
            LOGGER.info("Start game for " + player2.getUsername() + " failed: " + e.getMessage());
            lock.lock();
            freePlayers.put(player1.getUsername(), player1);
            playingPlayers.remove(player1.getUsername());
            playingPlayers.remove(player2.getUsername());
            lock.unlock();
            return;
        }
    }

}
