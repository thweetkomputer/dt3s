package client;

import client.rmi.ChatCallBackImpl;
import client.rmi.ChatCallBackInterface;
import client.rmi.GameCallBackImpl;
import client.rmi.GameCallBackInterface;
import common.PlaceholderTextField;
import lombok.NoArgsConstructor;
import server.rmi.ChatInterface;
import server.rmi.GameInterface;
import server.rmi.LoginInterface;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicLong;

/**
 * the client class, contains GUI.
 */
public class Client {
    private final String username;
    private final String ip;
    private final int port;
    private AtomicLong lastMessageTime = new AtomicLong(0);

    public Client(String username, String ip, int port) {
        this.username = username;
        this.ip = ip;
        this.port = port;
    }

    public void start() {
        LoginInterface loginInterface = null;
        ChatInterface chatInterface;
        GameInterface gameInterface;
        System.out.println("Connecting to server...");
        try {
            loginInterface = (LoginInterface) java.rmi.Naming.lookup("rmi://" + ip + ":" + port + "/login");
            gameInterface = (GameInterface) java.rmi.Naming.lookup("rmi://" + ip + ":" + port + "/game");
            chatInterface = (ChatInterface) java.rmi.Naming.lookup("rmi://" + ip + ":" + port + "/chat");
            GameCallBackInterface gameCallBackInterface = new GameCallBackImpl(lastMessageTime);
            ChatCallBackInterface chatCallBackInterface = new ChatCallBackImpl();
            String result = loginInterface.Login(username, gameCallBackInterface, chatCallBackInterface);
            // if login failed, print the reason and return.
            if (!result.equals("OK")) {
                System.out.println(result);
                return;
            }
            System.err.println("Login success.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Client exception: " + e);
            if (loginInterface != null) {
                try {
                    loginInterface.Logout(username);
                } catch (Exception ignored) {
                }
            }
            return;
        }

        SwingUtilities.invokeLater(this::createAndShowGUI);
        LoginInterface finalLoginInterface = loginInterface;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                finalLoginInterface.Logout(username);
            } catch (Exception ignored) {
            }
        }));
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Tic Tac Toe");
        frame.setSize(700, 500);
        frame.setMinimumSize(new Dimension(700, 500));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setBackground(Color.WHITE);
        frame.getContentPane().setBackground(Color.WHITE);
        findingPlayer(frame, 0);

        frame.setVisible(true);

    }

    private void findingPlayer(JFrame frame, long lastMessageTime) {
        frame.getContentPane().removeAll();
        frame.repaint();
        frame.revalidate();
        frame.setBackground(Color.WHITE);
        frame.getContentPane().setBackground(Color.WHITE);
        // Initialize JLabel and set the font
        JLabel label = new JLabel("Finding Player");
        label.setBackground(Color.WHITE);
        label.setHorizontalAlignment(JLabel.CENTER);

        // Set font style to bold, size to 24pt
        label.setFont(new Font("Comic Sans MS", Font.BOLD, 24));

        frame.add(label, BorderLayout.CENTER);

        // Initialize counter and Timer
        Timer timer;
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
                if (Client.this.lastMessageTime.get() != lastMessageTime) {
                    System.err.println("Found player");
                    ((Timer) (evt.getSource())).stop();
                    startGame(frame);
                }
            }
        };

        timer = new Timer(delay, taskPerformer);
        timer.start();
    }

    private void startGame(JFrame frame) {
        frame.getContentPane().removeAll();
        frame.repaint();
        frame.revalidate();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.WHITE);

        // timer panel
        JPanel timerPanel = new JPanel();
        timerPanel.setBackground(Color.WHITE);
        timerPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        timerPanel.setLayout(new BorderLayout());

        // timer label
        JLabel timerLabel = new JLabel("Timer", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 20));

        // timer value label
        JLabel timerValue = new JLabel("30", SwingConstants.CENTER);
        timerValue.setFont(new Font("Arial", Font.BOLD, 40));

        // Add labels to timer panel
        timerPanel.add(timerLabel, BorderLayout.NORTH);
        timerPanel.add(timerValue, BorderLayout.CENTER);

        timerPanel.setBounds(10, 10, 120, 100);
        mainPanel.add(timerPanel);

        // info label
        JLabel label = new JLabel("<html><div style='text-align: center;'>Distributed<br>Tic-Tac-Toe</div></html>");
        label.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);

        label.setBounds(20, 180, 120, 100);  // x, y, width, height
        mainPanel.add(label);

        JButton quitButton = new JButton("QUIT");
        quitButton.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        quitButton.setBackground(Color.WHITE);
        quitButton.addActionListener(e -> {
            System.exit(0);
        });
        quitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        quitButton.setBounds(20, 330, 100, 50); // x, y, width, height
        quitButton.setBorder(new LineBorder(Color.BLACK, 1));

        mainPanel.add(quitButton);

        JPanel boardWrapper = new JPanel();
        boardWrapper.setLayout(new BorderLayout());
        boardWrapper.setBounds(160, 80, 300, 300);  // x, y, width, height
        boardWrapper.setBackground(Color.WHITE);
        boardWrapper.setBorder(new CompoundBorder(
                new LineBorder(Color.BLACK, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));


        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3));
        boardPanel.setBackground(Color.WHITE);
        boardWrapper.add(boardPanel, BorderLayout.CENTER);

        JLabel turnLabel = new JLabel("Player 1's Turn");
        turnLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        turnLabel.setHorizontalAlignment(JLabel.CENTER);
        turnLabel.setBackground(Color.WHITE);
        turnLabel.setBounds(160, 40, 300, 40); // x, y, width, height

        for (int i = 0; i < 9; i++) {
            JButton btn = new JButton();
            CompoundBorder compoundBorder = new CompoundBorder(
                    new LineBorder(Color.BLACK, 1),
                    new EmptyBorder(10, 10, 10, 10)
            );
            btn.setBorder(compoundBorder);
            btn.setFont(new Font("Comic Sans MS", Font.BOLD, 60));
            btn.setBackground(Color.WHITE);
            boardPanel.add(btn);
        }

        boardPanel.setBorder(new LineBorder(Color.BLACK, 2));
        turnLabel.setBorder(new LineBorder(Color.BLACK, 1));

        JPanel combinedPanel = new JPanel();
        combinedPanel.setLayout(new BorderLayout());
        combinedPanel.setBorder(new LineBorder(Color.BLACK, 1));
        combinedPanel.setBounds(160, 40, 300, 340); // x, y, width, height
        combinedPanel.setBackground(Color.WHITE);
        combinedPanel.add(turnLabel, BorderLayout.NORTH);
        combinedPanel.add(boardWrapper, BorderLayout.CENTER);

        mainPanel.add(combinedPanel);

        // chat board
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setBorder(new LineBorder(Color.BLACK, 1));
        chatPanel.setBounds(480, 0, 220, 470); // x, y, width, height
        chatPanel.setBackground(Color.WHITE);

        // title label
        JLabel chatLabel = new JLabel("Player Chat");
        chatLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        chatLabel.setHorizontalAlignment(JLabel.CENTER);
        chatLabel.setBorder(new LineBorder(Color.BLACK, 1));
        chatLabel.setBackground(Color.WHITE);

        JTextArea chatTextArea = new JTextArea();
        chatTextArea.setEditable(false);

        // scroll pane
        JScrollPane chatScrollPane = new JScrollPane(chatTextArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JTextField chatInputField = new PlaceholderTextField("Type your message here...");
        chatInputField.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
        chatInputField.setBorder(null);

        Dimension dim = new Dimension(chatInputField.getPreferredSize().width, 30);
        chatInputField.setPreferredSize(dim);
        chatInputField.setMinimumSize(dim);
        chatInputField.setMaximumSize(dim);

        chatPanel.add(chatLabel, BorderLayout.NORTH);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(chatInputField, BorderLayout.SOUTH);

        mainPanel.add(chatPanel);

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}