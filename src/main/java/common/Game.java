// Chen Zhao 1427714
package common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

/**
 * A game room.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    private static final Logger LOGGER = Logger.getLogger(Game.class.getName());

    private char[][] board;
    private int turn;
    private int winner;
    private int timer = 20;
    private Player[] players;
    private String[] ranks;
    private TreeSet<Player> playerList;
    private String[] chess;
    private Lock mu;

    private Lock gameLock;

    private HashMap<String, Player> playingPlayers;

    /**
     * the constructor.
     *
     * @param username the player.
     */
    public boolean isMyTurn(String username) {
        gameLock.lock();
        var result = players[getTurn()].getUsername().equals(username);
        gameLock.unlock();
        return result;
    }

    /**
     * check if the move is valid.
     *
     * @param x the x position.
     * @param y the y position.
     * @return true if valid, false otherwise.
     */
    public boolean isValidMove(int x, int y) {
        gameLock.lock();
        var result = board[x][y] == ' ';
        gameLock.unlock();
        return result;
    }

    /**
     * move.
     *
     * @param x the x position.
     * @param y the y position.
     */
    public void move(int x, int y) {
        gameLock.lock();
        String turnLabel;
        board[x][y] = chess[turn].charAt(0);
        if (finished()) {
            if (winner == 2) {
                turnLabel = "Match Drawn";
            } else {
                String winnerName = players[winner].getUsername();
                turnLabel = "Player " + winnerName + " wins!";
            }
            reRank();
            LOGGER.info("Game finished");
            try {
                players[0].getClient().endGame(chess[turn], x, y, null, turnLabel);
                players[1].getClient().endGame(chess[turn], x, y, null, turnLabel);
            } catch (Exception ignored) {
            }
            gameLock.unlock();
            return;
        }
        turn = 1 - turn;
        timer = 20;
        turnLabel = getTurnLabel();
        String turnName = players[turn].getUsername();
        try {
            players[0].getClient().move(chess[1 - turn], x, y, turnName, turnLabel);
        } catch (Exception ignored) {
        }
        try {
            players[1].getClient().move(chess[1 - turn], x, y, turnName, turnLabel);
        } catch (Exception ignored) {
        }
        gameLock.unlock();
    }

    public String getTurnUser() {
        return players[turn].getUsername();
    }

    public String getTurnLabel() {
        return ranks[turn] + "'s turn (" + chess[turn] + ")";
    }

    /**
     * check if the game is finished.
     *
     * @return true if finished, false otherwise.
     */
    public boolean finished() {
        if (winner != -1) {
            return true;
        }
        var turn = getTurn();
        for (int i = 0; i < 3; ++i) {
            if (board[i][0] != ' ' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                winner = turn;
                return true;
            }
            if (board[0][i] != ' ' && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                winner = turn;
                return true;
            }
        }
        if (board[0][0] != ' ' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            winner = turn;
            return true;
        }
        if (board[0][2] != ' ' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            winner = turn;
            return true;
        }
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                if (board[j][i] == ' ') {
                    return false;
                }
            }
        }
        winner = 2;
        return true;
    }


    /**
     * stop the game.
     *
     * @param loser the loser.
     */
    public void stop(String loser) {
        gameLock.lock();
        if (players[0].getUsername().equals(loser)) {
            winner = 1;
        } else {
            winner = 0;
        }
        gameLock.unlock();
        reRank();
        var turnLabel = "Player " + players[winner].getUsername() + " wins!";

        try {
            players[winner].getClient().endGame(null, -1, -1, null, turnLabel);
        } catch (Exception ignored) {
        }
    }

    /**
     * draw the game.
     */
    private void draw() {
        gameLock.lock();
        winner = 2;
        gameLock.unlock();
        mu.lock();
        reRank();
        mu.unlock();
        var turnLabel = "Match Drawn";
        try {
            players[0].getClient().endGame(null, -1, -1, null, turnLabel);
        } catch (Exception ignored) {
        }
        try {
            players[1].getClient().endGame(null, -1, -1, null, turnLabel);
        } catch (Exception ignored) {
        }
    }

    public void heartbeat() {
        new Thread(() -> {
            int heartbeatElapsed = 0;
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                gameLock.lock();
                if (finished()) {
                    gameLock.unlock();
                    return;
                }
                var turnLabel = getTurnLabel();
                var turnUser = getTurnUser();
                gameLock.unlock();
                try {
                    players[0].getClient().heartbeat();
                    players[1].getClient().heartbeat();
                    if (heartbeatElapsed > 0) {
                        LOGGER.info("Heartbeat success");
                        heartbeatElapsed = 0;
                        players[0].getClient().setLabel(turnLabel, true);
                        players[1].getClient().setLabel(turnLabel, true);
                        players[0].getClient().continueGame(board, turnUser, turnLabel);
                        players[1].getClient().continueGame(board, turnUser, turnLabel);
                    }
                } catch (Exception e) {
                    try {
                        players[0].getClient().setLabel("Opponent disconnected", true);
                    } catch (RemoteException ignored) {
                    }
                    try {
                        players[1].getClient().setLabel("Opponent disconnected", true);
                    } catch (RemoteException ignored) {
                    }
                    heartbeatElapsed++;
                    if (heartbeatElapsed > 30) {
                        draw();
                        return;
                    }
                }
            }
        }).start();
    }

    private void reRank() {
        mu.lock();
        playingPlayers.remove(players[0].getUsername());
        playingPlayers.remove(players[1].getUsername());
        players[0].setGame(null);
        players[1].setGame(null);
        playerList.remove(players[0]);
        playerList.remove(players[1]);
        if (winner < 2 && winner >= 0) {
            players[winner].win();
            players[1 - winner].lose();
        } else {
            players[0].draw();
            players[1].draw();
        }
        playerList.add(players[0]);
        playerList.add(players[1]);
        int rank = 1;
        for (Player player : playerList) {
            player.setRank(rank++);
        }

        mu.unlock();
    }
}
