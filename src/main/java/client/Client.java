package client;

import client.rmi.ChatCallBackImpl;
import client.rmi.ChatCallBackInterface;
import server.rmi.ChatInterface;
import server.rmi.GameInterface;
import server.rmi.LoginInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

    public void start() {
        LoginInterface loginInterface;
        ChatInterface chatInterface;
        GameInterface gameInterface;
        System.out.println("Connecting to server...");
        try {
            loginInterface = (LoginInterface) java.rmi.Naming.lookup("rmi://" + ip + ":" + port + "/login");
            gameInterface = (GameInterface) java.rmi.Naming.lookup("rmi://" + ip + ":" + port + "/game");
            chatInterface = (ChatInterface) java.rmi.Naming.lookup("rmi://" + ip + ":" + port + "/chat");
            ChatCallBackInterface chatCallBackInterface = new ChatCallBackImpl();
            chatInterface.register(chatCallBackInterface);
            String result = loginInterface.Login(username);
            // if login failed, print the reason and return.
            if (!result.equals("OK")) {
                System.out.println(result);
                return;
            }
            System.out.println("Login success.");
        } catch (Exception e) {
            System.err.println("Client exception: " + e);
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
        JFrame frame = new JFrame("Tic Tac Toe");
        frame.setSize(500, 300);
        frame.setMinimumSize(new Dimension(500, 300));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Initialize JLabel and set the font
        JLabel label = new JLabel("Finding Player");
        label.setHorizontalAlignment(JLabel.CENTER);

        // Set font style to bold, size to 24pt
        label.setFont(new Font("Comic Sans MS", Font.BOLD, 24));

        frame.add(label, BorderLayout.CENTER);

        // Initialize counter and Timer
        int delay = 500;
        ActionListener taskPerformer = new ActionListener() {
            private int dotCount = 0;

            public void actionPerformed(ActionEvent evt) {
                dotCount++;
                label.setText("Finding Player" + ".".repeat(Math.max(0, dotCount)));

                // Reset dotCount if it reaches 3
                if (dotCount >= 3) {
                    dotCount = 0;
                }
            }
        };

        new Timer(delay, taskPerformer).start();

        frame.setVisible(true);


    }
}