// Chen Zhao 1427714
package common;

import client.rmi.GameCallBackInterface;
import exception.GameException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private GameCallBackInterface[] clients;
    private Lock gameLock;

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
     * move.
     *
     * @param x the x position.
     * @param y the y position.
     * @throws GameException the game exception.
     */
    public void move(int x, int y) throws GameException {
        gameLock.lock();
        String turnLabel;
        board[x][y] = chess[turn].charAt(0);
        if (finished()) {
            if (winner == -1) {
                turnLabel = "Match Drawn";
            } else {
                String winnerName = players[winner].getUsername();
                turnLabel = "Player " + winnerName + " wins!";
            }
            reRank();
            gameLock.unlock();
            try {
                clients[0].move(chess[turn], x, y, null, turnLabel);
            } catch (Exception e) {
                gameLock.unlock();
                throw new GameException(players[0].getUsername());
            }
            try {
                clients[1].move(chess[turn], x, y, null, turnLabel);
            } catch (Exception e) {
                gameLock.unlock();
                throw new GameException(players[1].getUsername());
            }
            try {
                clients[0].ask();
            } catch (Exception e) {
                gameLock.unlock();
                throw new GameException(players[0].getUsername());
            }
            try {
                clients[1].ask();
            } catch (Exception e) {
                gameLock.unlock();
                throw new GameException(players[1].getUsername());
            }
            return;
        }
        turn = 1 - turn;
        timer = 20;
        turnLabel = getTurnLabel();
        String turnName = players[turn].getUsername();
        try {
            clients[0].setTimer(timer, null);
            clients[0].move(chess[1 - turn], x, y, turnName, turnLabel);
        } catch (Exception e) {
            throw new GameException(players[0].getUsername());
        }
        try {
            clients[1].setTimer(timer, null);
            clients[1].move(chess[1 - turn], x, y, turnName, turnLabel);
        } catch (Exception e) {
            throw new GameException(players[1].getUsername());
        }
        gameLock.unlock();
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
        gameLock.lock();
        try {
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
            winner = -1;
            return true;
        } finally {
            gameLock.unlock();
        }
    }


    /**
     * stop the game.
     *
     * @param loser the loser.
     */
    public void stop(String loser) throws GameException {
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
            clients[winner].setLabel(turnLabel);
        } catch (Exception e) {
            LOGGER.info("Set label failed: " + e.getMessage());
            throw new GameException(players[winner].getUsername());
        }

        try {
            clients[winner].ask();
        } catch (Exception e) {
            throw new GameException(players[winner].getUsername());
        }
    }

    private void reRank() {
        players[0].setGame(null);
        players[1].setGame(null);
        playerList.remove(players[0]);
        playerList.remove(players[1]);
        if (winner != -1) {
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
            player.rank = rank++;
        }
    }
}
