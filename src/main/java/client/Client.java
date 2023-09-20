// Chen Zhao 1427714
package client;

import client.rmi.GameCallBackImpl;
import client.rmi.GameCallBackInterface;
import common.MemoryTextArea;
import common.PlaceholderTextField;
import server.rmi.GameInterface;
import server.rmi.LoginInterface;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.System.exit;

/**
 * the client class, contains GUI.
 */
public class Client {
    private final String username;
    private final String ip;
    private final int port;
    private AtomicLong lastMessageTime = new AtomicLong(0);

    LoginInterface loginService;
    GameInterface gameService;
    private ReadWriteLock mu = new ReentrantReadWriteLock();
    private JLabel turnLabel = new JLabel();

    private JTextArea chatTextArea = new MemoryTextArea(10);

    private JButton[] boards = new JButton[9];

    {
        for (int i = 0; i < boards.length; i++) {
            boards[i] = new JButton();
            boards[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    /**
     * Instantiates a new Client.
     *
     * @param username the username.
     * @param ip       the ip.
     * @param port     the port.
     */
    public Client(String username, String ip, int port) {
        this.username = username;
        this.ip = ip;
        this.port = port;
    }

    public void start() {
        mu.writeLock().lock();
        try {
            createAndShowGUI();
        } catch (Exception e) {
            e.printStackTrace();
            // TODO
        }
        mu.writeLock().unlock();
        System.out.println("UI created.");
        System.out.println("Connecting to server...");
        try {
            loginService = (LoginInterface) java.rmi.Naming.lookup("rmi://" + ip + ":" + port + "/login");
            gameService = (GameInterface) java.rmi.Naming.lookup("rmi://" + ip + ":" + port + "/game");

            // bind click
            for (int i = 0; i < 9; ++i) {
                JButton button = boards[i];
                int finalI = i;
                button.addActionListener(e -> {
                    try {
                        System.out.println("click " + finalI);
                        gameService.makeMove(username, finalI / 3, finalI % 3);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                });
            }

            GameCallBackInterface gameCallBackInterface = new GameCallBackImpl(mu, lastMessageTime, boards, turnLabel,
                    chatTextArea, gameService, username);

            loginService.Login(username, gameCallBackInterface);
            System.err.println("Login success.");
            GameCallBackImpl.findingPlayer(500, mu, turnLabel, gameService, username);
        } catch (Exception e) {
            System.err.println("Client exception: " + e);
            if (loginService != null) {
                try {
                    loginService.Logout(username);
                } catch (Exception ignored) {
                }
            }
            exit(0);
        }

        LoginInterface finalLoginService = loginService;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                finalLoginService.Logout(username);
            } catch (Exception ignored) {
            }
        }));
    }

    private void createAndShowGUI() throws Exception {
        JFrame frame = new JFrame("Tic Tac Toe");
        frame.setSize(700, 500);
        frame.setMinimumSize(new Dimension(700, 500));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setBackground(Color.WHITE);
        frame.getContentPane().setBackground(Color.WHITE);
        startGame(frame, 0);

        frame.setVisible(true);

    }

    private void startGame(JFrame frame, long lastMessageTime) throws Exception {
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
            exit(0);
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
        turnLabel.setText("Finding Player");
        turnLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        turnLabel.setHorizontalAlignment(JLabel.CENTER);
        turnLabel.setBackground(Color.WHITE);
        turnLabel.setBounds(160, 40, 300, 40); // x, y, width, height
        turnLabel.setBackground(Color.WHITE);
        turnLabel.setHorizontalAlignment(JLabel.CENTER);


        // draw board
        for (int i = 0; i < 9; i++) {
            JButton btn = boards[i];
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
        chatLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
        chatLabel.setHorizontalAlignment(JLabel.CENTER);
        chatLabel.setBorder(new LineBorder(Color.BLACK, 1));
        chatLabel.setBackground(Color.WHITE);

        chatTextArea.setEditable(false);
        chatTextArea.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
        chatTextArea.setLineWrap(true);
        chatTextArea.setWrapStyleWord(true);
        // TODO: from bottom

        // scroll pane
        JScrollPane chatScrollPane = new JScrollPane(chatTextArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JTextField chatInputField = new PlaceholderTextField("Type your message here...");
        chatInputField.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        chatInputField.setBorder(null);
        chatInputField.addActionListener(e -> {
            String message = chatInputField.getText();
            if (!message.trim().isEmpty()) {
                try {
                    gameService.sendMessage(username, message);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                chatInputField.setText("");
            }
            chatTextArea.setCaretPosition(chatTextArea.getDocument().getLength());
        });

        Dimension dim = new Dimension(chatInputField.getPreferredSize().width, 45);
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