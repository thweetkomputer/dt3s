// Chen Zhao 1427714
package client.rmi;

import client.Client;
import lombok.Data;
import server.rmi.GameInterface;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

/**
 * the game callback implementation.
 */
@Data
public class GameCallBackImpl extends UnicastRemoteObject implements GameCallBackInterface {
    private static final Logger LOGGER = Logger.getLogger(GameCallBackImpl.class.getName());

    private AtomicLong lastMessageTime = null;

    private JLabel turnLabel = null;
    private JButton[] board = null;
    private JTextArea textArea = null;
    private JLabel timerValue = null;
    private JDialog disconnectedDialog = null;

    private Lock mu = null;

    private GameInterface service = null;

    private String username = null;

    private Client client = null;


    /**
     * the constructor.
     */
    public GameCallBackImpl() throws Exception {
        super();
    }

    /**
     * the constructor.
     */
    public GameCallBackImpl(Lock mu, AtomicLong lastMessageTime, JButton[] board, JLabel turnLabel,
                            JTextArea textArea, JLabel timerValue, GameInterface service, String username,
                            Client client) throws RemoteException {
        super();
        this.mu = mu;
        this.lastMessageTime = lastMessageTime;
        this.turnLabel = turnLabel;
        this.board = board;
        this.textArea = textArea;
        this.timerValue = timerValue;
        this.service = service;
        this.username = username;
        this.client = client;
    }

    /**
     * start the game.
     *
     * @param opponent the opponent.
     * @param turn     the turn.
     * @param label    the label.
     * @throws RemoteException the remote exception.
     */
    @Override
    public void startGame(String opponent, String turn, String label) throws RemoteException {
        LOGGER.info("start game with " + opponent);
        mu.lock();
        turnLabel.setText(label);
        lastMessageTime.set(System.currentTimeMillis());
        textArea.setText(null);
        mu.unlock();
        startTimer(lastMessageTime.get(), turn);
    }

    /**
     * move.
     *
     * @param chess the chess.
     * @param x     the x coordinate.
     * @param y     the y coordinate.
     * @param turn  the turn.
     * @param label the label.
     * @throws RemoteException the remote exception.
     */
    @Override
    public void move(String chess, int x, int y, String turn, String label) throws RemoteException {
        mu.lock();
        board[x * 3 + y].setText(chess);
        turnLabel.setText(label);
        lastMessageTime.set(System.currentTimeMillis());
        var lastMessageTime = this.lastMessageTime.get();
        mu.unlock();
        startTimer(lastMessageTime, turn);
    }

    @Override
    public void endGame(String chess, int x, int y, String turn, String label) throws RemoteException {
        mu.lock();
        if (chess != null) {
            board[x * 3 + y].setText(chess);
        }
        turnLabel.setText(label);
        lastMessageTime.set(System.currentTimeMillis());
        mu.unlock();
        ask();
    }

    /**
     * continue
     *
     * @param board the board.
     * @param turn  the turn.
     * @param label the label.
     * @throws RemoteException the remote exception.
     */
    @Override
    public void continueGame(char[][] board, String turn, String label) throws RemoteException {
        mu.lock();
        for (int i = 0; i < 9; ++i) {
            this.board[i].setText(String.valueOf(board[i / 3][i % 3]));
        }
        turnLabel.setText(label);
        lastMessageTime.set(System.currentTimeMillis());
        var lastMessageTime = this.lastMessageTime.get();
        mu.unlock();
        startTimer(lastMessageTime, turn);
    }

    /**
     * heartbeat.
     */
    @Override
    public void heartbeat() {
    }

    /**
     * start timer.
     *
     * @param lastMessageTime the last message time.
     * @param turn            the turn.
     */
    public void startTimer(long lastMessageTime, String turn) {
        if (!turn.equals(username)) {
            mu.lock();
            timerValue.setText("20");
            mu.unlock();
            return;
        }
        // start timer
        new Thread(() -> {
            var timer = 20;
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                if (timer == 0) {
                    // random choice
                    java.util.List<Integer> available = new ArrayList<>();
                    mu.lock();
                    for (int i = 0; i < 9; ++i) {
                        if (board[i].getText().isEmpty()) {
                            available.add(i);
                        }
                    }
                    mu.unlock();
                    if (available.isEmpty()) {
                        continue;
                    }
                    int random = (int) (Math.random() * available.size());
                    int index = available.get(random);
                    int x1 = index / 3;
                    int y1 = index % 3;
                    try {
                        service.makeMove(username, x1, y1);
                    } catch (Exception e) {
                        LOGGER.info("Make move failed: " + e.getMessage());
                    }
                }
                try {
                    if (!setTimer(timer--, lastMessageTime)) {
                        return;
                    }
                } catch (Exception ignored) {
                }
            }
        }).start();
    }

    /**
     * set label.
     *
     * @param label      the label.
     * @param resetTimer whether reset timer.
     * @throws RemoteException the remote exception.
     */
    @Override
    public void setLabel(String label, boolean resetTimer) throws RemoteException {
        mu.lock();
        if (resetTimer) {
            lastMessageTime.set(System.currentTimeMillis());
        }
        turnLabel.setText(label);
        mu.unlock();
    }

    /**
     * send message.
     *
     * @param message the message.
     * @throws RemoteException the remote exception.
     */
    @Override
    public void send(String message) throws RemoteException {
        mu.lock();
        textArea.append(message + "\n");
        mu.unlock();
    }

    public void ask() {
        lastMessageTime.set(System.currentTimeMillis());
        SwingUtilities.invokeLater(() -> {
            JOptionPane pane = getjOptionPane();

            JDialog dialog = pane.createDialog(null, "Choose an option");
            dialog.setIconImage(null);
            dialog.setVisible(true);

            String value = pane.getValue().toString();
            int delay = 500;
            if ("find".equals(value)) {
                mu.lock();
                for (int i = 0; i < 9; ++i) {
                    board[i].setText("");
                }
                mu.unlock();
                client.findingPlayer(delay, mu, turnLabel);
                try {
                    service.findGame(username, this);
                } catch (RemoteException ignored) {
                }
            } else if ("quit".equals(value)) {
                System.exit(0);
            }
        });
    }

    private static JOptionPane getjOptionPane() {
        JOptionPane pane = new JOptionPane();
        pane.setMessageType(JOptionPane.QUESTION_MESSAGE);
        pane.setOptionType(JOptionPane.YES_NO_OPTION);
        JLabel label = new JLabel("Find next player?");
        label.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        pane.setMessage(label);

        JButton findButton = new JButton("find");
        findButton.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        findButton.addActionListener(e -> pane.setValue("find"));

        JButton quitButton = new JButton("quit");
        quitButton.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        quitButton.addActionListener(e -> pane.setValue("quit"));

        pane.setOptions(new Object[]{quitButton, findButton});
        return pane;
    }

    public boolean setTimer(int timer, Long lastMessageTime) throws RemoteException {
        mu.lock();
        if (lastMessageTime != null && lastMessageTime != this.lastMessageTime.get()) {
            mu.unlock();
            return false;
        }
        if (timer < 0) {
            mu.unlock();
            return false;
        }
        timerValue.setText(String.valueOf(timer));
        mu.unlock();
        return true;
    }
}
