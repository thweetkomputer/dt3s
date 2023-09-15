package server;

import lombok.AllArgsConstructor;
import server.rmi.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * the server class.
 */
@AllArgsConstructor
public class Server {
    private String ip;
    private int port;

    public void start() {
        try {
            startRegistry();
            registerService(new LoginImpl(), "login");
            registerService(new GameImpl(), "game");
            registerService(new ChatImpl(), "chat");
        } catch (Exception e) {
            System.err.println("Server exception: " + e);
            return;
        }
        System.out.println("Server started.");
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
