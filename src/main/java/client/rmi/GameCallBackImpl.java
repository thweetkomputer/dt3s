package client.rmi;

import lombok.Data;
import server.rmi.GameInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * the game callback implementation.
 */
@Data
public class GameCallBackImpl extends UnicastRemoteObject implements GameCallBackInterface {

    private AtomicLong lastMessageTime = null;

    private JLabel turnLabel = null;
    private JButton[] board = null;
    private JTextArea textArea = null;

    private ReadWriteLock mu = null;

    private GameInterface service = null;

    private String username = null;


    /**
     * the constructor.
     */
    public GameCallBackImpl() throws Exception {
        super();
    }

    /**
     * the constructor.
     */
    public GameCallBackImpl(ReadWriteLock mu, AtomicLong lastMessageTime, JButton[] board, JLabel turnLabel,
                            JTextArea textArea, GameInterface service, String username) throws Exception {
        super();
        this.mu = mu;
        this.lastMessageTime = lastMessageTime;
        this.turnLabel = turnLabel;
        this.board = board;
        this.textArea = textArea;
        this.service = service;
        this.username = username;
    }

    /**
     * start the game.
     *
     * @param opponent    the opponent.
     * @param messageTime the message time, used to stop the timer.
     * @param turn        the turn.
     */
    @Override
    public void startGame(String opponent, long messageTime, String turn) {
        System.out.println("start game with " + opponent);
        mu.writeLock().lock();
        this.lastMessageTime.set(messageTime);
        this.turnLabel.setText(turn);
        mu.writeLock().unlock();
    }

    /**
     * move.
     *
     * @param chess the chess.
     * @param x     the x coordinate.
     * @param y     the y coordinate.
     * @param turn  the turn.
     */
    @Override
    public void move(String chess, int x, int y, String turn) {
        mu.writeLock().lock();
        this.board[x * 3 + y].setText(chess);
        this.turnLabel.setText(turn);
        mu.writeLock().unlock();
    }

    /**
     * send message.
     *
     * @param message the message.
     */
    @Override
    public void send(String message) {
        mu.writeLock().lock();
        textArea.append(message + "\n");
        mu.writeLock().unlock();
    }

    /**
     * ask for a rematch.
     */
    @Override
    public void ask() {
        SwingUtilities.invokeLater(() -> {
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

            JDialog dialog = pane.createDialog(null, "Choose an option");
            dialog.setIconImage(null);
            dialog.setVisible(true);

            String value = pane.getValue().toString();
            int delay = 500;
            if ("find".equals(value)) {
                for (int i = 0; i < 9; ++i) {
                    board[i].setText("");
                }
                try {
                    findingPlayer(delay, mu, turnLabel, service, username);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                    // TODO
                }
            } else if ("quit".equals(value)) {
                System.exit(0);
            }
        });

    }

    public static void findingPlayer(int delay, ReadWriteLock mu, JLabel turnLabel, GameInterface service,
                                     String username) throws Exception {
        mu.writeLock().lock();
        turnLabel.setText("Finding Player");
        mu.writeLock().unlock();
        Timer timer;
        ActionListener taskPerformer = new ActionListener() {
            private int dotCount = 0;

            public void actionPerformed(ActionEvent evt) {
                mu.readLock().lock();
                if (!turnLabel.getText().startsWith("Finding Player")) {
                    ((Timer) (evt.getSource())).stop();
                    mu.readLock().unlock();
                    return;
                }
                mu.readLock().unlock();
                dotCount++;
                mu.writeLock().lock();
                turnLabel.setText("Finding Player" + ".".repeat(Math.max(0, dotCount)));
                // reset dotCount if it reaches 3
                if (dotCount >= 3) {
                    dotCount = 0;
                }
                mu.writeLock().unlock();
            }
        };

        timer = new Timer(delay, taskPerformer);
        timer.start();
        service.findGame(username);
    }
}
