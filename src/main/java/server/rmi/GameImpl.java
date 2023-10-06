// Chen Zhao 1427714
package server.rmi;

import client.rmi.GameCallBackInterface;
import common.Game;
import common.Player;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * the game implementation.
 */
public class GameImpl extends UnicastRemoteObject implements GameInterface {
    private static final Logger LOGGER = Logger.getLogger(GameImpl.class.getName());

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
                    Lock lock) throws RemoteException {
        super();
        this.allPlayers = players;
        this.playerList = playerList;
        this.freePlayers = freePlayers;
        this.playingPlayers = playingPlayers;
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
    public String makeMove(String username, int x, int y) throws RemoteException{
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
     */
    @Override
    public void sendMessage(String username, String message) throws RemoteException{
        lock.lock();
        Player player = playingPlayers.get(username);
        if (player == null) {
            return;
        }
        Game game = player.getGame();
        if (game == null) {
            return;
        }
        message = player + ": " + message;
        try {
            game.getPlayers()[0].getClient().send(message);
            game.getPlayers()[1].getClient().send(message);
        } catch (Exception ignored) {
        }
        lock.unlock();
    }

    /**
     * find a game.
     *
     * @param username the username.
     */
    @Override
    public void findGame(String username, GameCallBackInterface client) throws RemoteException {
        Player player = allPlayers.get(username);
        LOGGER.info("Player " + username + " find game");
        lock.lock();
        if (player == null) {
            player = new Player(username, allPlayers.size() + 1, System.currentTimeMillis(), client);
            allPlayers.put(username, player);
            playerList.add(player);
        } else {
            player.setClient(client);
        }
        if (playingPlayers.containsKey(username)) {
            lock.unlock();
            LOGGER.info("Player " + username + " is playing");
            return;
        }
        freePlayers.put(username, player);
        LOGGER.info("Player " + username + " is free");
        if (freePlayers.size() > 1) {
            Player player1 = freePlayers.values().iterator().next();
            freePlayers.remove(player1.getUsername());
            Player player2 = freePlayers.values().iterator().next();
            freePlayers.remove(player2.getUsername());
            playingPlayers.put(player1.getUsername(), player1);
            playingPlayers.put(player2.getUsername(), player2);
            // start game
            LOGGER.info("Start game " + player1.getUsername() + " vs " + player2.getUsername() + ".");
            executor.execute(() -> startGame(player1, player2));
        }
        lock.unlock();
    }

    /**
     * quit a game.
     *
     * @param username the username.
     * @throws RemoteException the remote exception.
     */
    @Override
    public void quit(String username) throws RemoteException {
        lock.lock();
        freePlayers.remove(username);
        if (playingPlayers.get(username) != null) {
            try {
                LOGGER.info("Player " + username + " stop game.");
                playingPlayers.get(username).getGame().stop(username);
            } catch (Exception ignored) {
            }
            playingPlayers.remove(username);
        }
        lock.unlock();
        LOGGER.info("Player " + username + " logout.");
    }

    /**
     * heartbeat.
     */
    @Override
    public void heartbeat() throws RemoteException {
    }

    private void startGame(Player player1, Player player2) {
        // start game
        Game game;
        lock.lock();
        LOGGER.info("Start game between " + player1.getUsername() + " and " + player2.getUsername() + ".");
        playingPlayers.put(player1.getUsername(), player1);
        playingPlayers.put(player2.getUsername(), player2);
        Player[] players = new Player[]{player1, player2};
        String[] chess;
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
                playerList, chess, lock, new ReentrantLock(), playingPlayers);
        player1.setGame(game);
        player2.setGame(game);
        var turnLabel = game.getTurnLabel();
        lock.unlock();
        try {
            player1.getClient().startGame(player2.getUsername(), players[turn].getUsername(), turnLabel);
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
            player2.getClient().startGame(player1.getUsername(), players[turn].getUsername(), turnLabel);
        } catch (RemoteException e) {
            LOGGER.info("Start game for " + player2.getUsername() + " failed: " + e.getMessage());
            lock.lock();
            freePlayers.put(player1.getUsername(), player1);
            playingPlayers.remove(player1.getUsername());
            playingPlayers.remove(player2.getUsername());
            lock.unlock();
            return;
        }
        game.heartbeat();
    }

}
