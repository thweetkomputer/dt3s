// Chen Zhao 1427714
package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import common.MyReentrantLock;
import common.Player;
import server.rmi.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

/**
 * the server class.
 */
public class Server {
    private static final Logger LOGGER = Logger.getLogger(GameImpl.class.getName());

    private final String ip;
    private final int port;

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
     * lock.
     */
    private final Lock lock = new MyReentrantLock();



    public void start() {
        startRegistry();
        startServices();

        LOGGER.info("Server starting ...");

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            server.createContext("/", exchange -> {
                StringBuilder response = new StringBuilder();
                lock.lock();
                for (Player player : playerList) {
                    response.append(player.getUsername()).append(" ").append(player.getScore()).append("\n");
                }
                lock.unlock();

                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.toString().getBytes());
                os.close();
            });

            server.start();
        } catch (IOException ignored) {
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
            GameInterface gameService = new GameImpl(players, playerList, freePlayers, playingPlayers, lock);
            registerService(gameService);
        } catch (RemoteException | MalformedURLException e) {
            LOGGER.info("Server register service failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private void registerService(Remote service) throws RemoteException, MalformedURLException {
        String url = String.format("rmi://%s:%d/%s", ip, port, "game");
        Naming.rebind(url, service);
        System.out.println("Service " + "game" + " bound to " + url);
    }


}
