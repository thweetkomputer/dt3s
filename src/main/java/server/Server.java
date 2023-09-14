package server;

import lombok.AllArgsConstructor;

import java.rmi.Naming;
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
            LocateRegistry.createRegistry(port);
            LoginImpl login = new LoginImpl();
            Naming.rebind("rmi://" + ip + ":" + port + "/login", login);
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            return;
        }
        System.out.println("Server started.");
    }
}
