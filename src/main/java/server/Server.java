package server;

import client.rmi.GameCallBackInterface;
import common.Player;
import lombok.AllArgsConstructor;
import server.rmi.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * the server class.
 */
@AllArgsConstructor
public class Server {
    private static final Logger LOGGER = Logger.getLogger(GameImpl.class.getName());

    private String ip;
    private int port;

    public void start() {
        ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Player> freePlayers = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Player> playingPlayers = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, GameCallBackInterface> gameClients = new ConcurrentHashMap<>();
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        try {
            startRegistry();
            registerService(
                    new LoginImpl(players, freePlayers, playingPlayers, gameClients, lock, condition),
                    "login");
            GameImpl gameService = new GameImpl(freePlayers, playingPlayers, gameClients, lock, condition);
            registerService(gameService, "game");
            gameService.Start();
        } catch (Exception e) {
            System.err.println("Server exception: " + e);
            return;
        }
        LOGGER.info("Server started.");
    }

    private void startRegistry() throws RemoteException {
        LocateRegistry.createRegistry(port);
    }

    private void registerService(Remote service, String serviceName) throws RemoteException, MalformedURLException {
        String url = String.format("rmi://%s:%d/%s", ip, port, serviceName);
        Naming.rebind(url, service);
        System.out.println("Service " + serviceName + " bound to " + url);
    }
}
