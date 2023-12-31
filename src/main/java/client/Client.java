// Chen Zhao 1427714
package client;

import client.rmi.GameCallBackImpl;
import client.rmi.GameCallBackInterface;
import common.MemoryTextArea;
import common.PlaceholderTextField;
import server.rmi.GameInterface;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static java.lang.System.exit;

/**
 * the client class, contains GUI.
 */
public class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private final String username;
    private final String ip;
    private final int port;
    private final AtomicLong lastMessageTime = new AtomicLong(0);

    GameInterface gameService;
    private final Lock mu = new ReentrantLock();
    private final JLabel turnLabel = new JLabel();

    private final JTextArea chatTextArea = new MemoryTextArea(10);

    private JLabel timerValue = new JLabel("20", SwingConstants.CENTER);

    private final JButton[] boards = new JButton[9];

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
        mu.lock();
        try {
            createAndShowGUI();
        } catch (Exception e) {
            LOGGER.info("Create GUI failed: " + e.getMessage());
            return;
        }
        mu.unlock();
        System.out.println("UI created.");
        System.out.println("Connecting to server...");
        try {
            gameService = (GameInterface) java.rmi.Naming.lookup("rmi://" + ip + ":" + port + "/game");

            // bind click
            for (int i = 0; i < 9; ++i) {
                JButton button = boards[i];
                int finalI = i;
                button.addActionListener(e -> {
                    try {
                        if (!"OK".equals(gameService.makeMove(username, finalI / 3, finalI % 3))) {
                            LOGGER.info("Make move failed: " + username);
                        }
                    } catch (Exception ignored) {
                    }
                });
            }

            GameCallBackInterface gameCallBackInterface = new GameCallBackImpl(mu, lastMessageTime, boards, turnLabel,
                    chatTextArea, timerValue, gameService, username, this);

            findingPlayer(500, mu, turnLabel);
            gameService.findGame(username, gameCallBackInterface);
        } catch (RemoteException | NotBoundException | MalformedURLException ignored) {
        }

        GameInterface finalGameService = gameService;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                finalGameService.quit(username);
            } catch (Exception ignored) {
            }
        }));

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            try {
                gameService.heartbeat();
            } catch (Exception e) {
                gracefulExit();
            }
        }
    }

    private void createAndShowGUI() throws Exception {
        JFrame frame = new JFrame("Tic Tac Toe  - " + username);
        frame.setSize(700, 500);
        frame.setMinimumSize(new Dimension(700, 500));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setBackground(Color.WHITE);
        frame.getContentPane().setBackground(Color.WHITE);
        startGame(frame);

        frame.setVisible(true);


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
        timerValue = new JLabel("20", SwingConstants.CENTER);
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
        quitButton.addActionListener(e -> exit(0));
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

        // scroll pane
        JScrollPane chatScrollPane = new JScrollPane(chatTextArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JTextField chatInputField = getjTextField();

        chatPanel.add(chatLabel, BorderLayout.NORTH);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(chatInputField, BorderLayout.SOUTH);

        mainPanel.add(chatPanel);

        frame.add(mainPanel);
    }

    private JTextField getjTextField() {
        JTextField chatInputField = new PlaceholderTextField("Type your message here...");
        chatInputField.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        chatInputField.setBorder(null);
        chatInputField.addActionListener(e -> {
            String message = chatInputField.getText();
            if (!message.trim().isEmpty()) {
                try {
                    gameService.sendMessage(username, message);
                } catch (Exception ignored) {
                }
                chatInputField.setText("");
            }
            chatTextArea.setCaretPosition(chatTextArea.getDocument().getLength());
        });

        Dimension dim = new Dimension(chatInputField.getPreferredSize().width, 45);
        chatInputField.setPreferredSize(dim);
        chatInputField.setMinimumSize(dim);
        chatInputField.setMaximumSize(dim);
        return chatInputField;
    }

    public void findingPlayer(int delay, Lock mu, JLabel turnLabel) {
        mu.lock();
        turnLabel.setText("Finding Player");
        mu.unlock();
        Timer timer;
        ActionListener taskPerformer = new ActionListener() {
            private int dotCount = 0;

            public void actionPerformed(ActionEvent evt) {
                mu.lock();
                if (!turnLabel.getText().startsWith("Finding Player")) {
                    ((Timer) (evt.getSource())).stop();
                    mu.unlock();
                    return;
                }
                dotCount++;
                turnLabel.setText("Finding Player" + ".".repeat(Math.max(0, dotCount)));
                // reset dotCount if it reaches 3
                if (dotCount >= 3) {
                    dotCount = 0;
                }
                mu.unlock();
            }
        };

        timer = new Timer(delay, taskPerformer);
        timer.start();
    }

    public void gracefulExit() {
        turnLabel.setText("Server unavailable");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }
        System.exit(0);
    }
}