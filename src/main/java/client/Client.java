package client;

import exception.LoginException;
import lombok.AllArgsConstructor;
import rmi.LoginInterface;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

/**
 * the client class, contains GUI.
 */
public class Client {
    private final String username;
    private final String ip;
    private final int port;

    public Client(String username, String ip, int port) {
        this.username = username;
        this.ip = ip;
        this.port = port;
    }

    public void start() throws Exception {
        System.out.println("Client started.");
        LoginInterface loginInterface = (LoginInterface) java.rmi.Naming.lookup("rmi://" + ip + ":" + port + "/login");
        try {
            String result = loginInterface.Login(username);
            // if login failed, print the reason and return.
            if (!result.equals("OK")) {
                System.out.println(result);
                return;
            }
        } catch (RemoteException e) {
            System.err.println(e.getMessage());
            return;
        }
        SwingUtilities.invokeLater(this::createAndShowGUI);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                loginInterface.Logout(username);
            } catch (Exception ignored) {
            }
        }));
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Client");
        frame.setSize(500, 300);
        frame.setMinimumSize(new Dimension(500, 300));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }
}