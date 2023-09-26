// Chen Zhao 1427714
package common;

import client.rmi.GameCallBackInterface;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.rmi.RemoteException;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;

/**
 * A game room.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {
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
     * @throws RemoteException the remote exception.
     */
    public void move(int x, int y) throws RemoteException {
        gameLock.lock();
        String turnLabel;
        board[x][y] = chess[turn].charAt(0);
        if (finished()) {
            if (winner == -1) {
                turnLabel = "Match Drawn";
            } else {
                turnLabel = "Player " + players[winner].getUsername() + " wins!";
            }
            clients[0].move(chess[turn], x, y, turnLabel);
            clients[1].move(chess[turn], x, y, turnLabel);
            mu.lock();
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
            mu.unlock();
            gameLock.unlock();
            clients[0].ask();
            clients[1].ask();
            return;
        }
        turn = 1 - turn;
        turnLabel = getTurnLabel();
        clients[0].move(chess[1 - turn], x, y, turnLabel);
        clients[1].move(chess[1 - turn], x, y, turnLabel);
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
    }

    /**
     * start the game.
     */
    public void start() throws RemoteException {
        // TODO
        while (true) {
            var turn = getTurn();
            if (timer < 0) {
                winner = 1 - turn;
                mu.lock();
                players[winner].win();
                players[1 - winner].lose();
                var turnLabel = "Player " + players[winner].getUsername() + " wins!";
                clients[0].setLabel(turnLabel);
                clients[1].setLabel(turnLabel);
                mu.unlock();
                break;
            }
            while (timer >= 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                timer--;
                // TODO check if the player is still online
                clients[0].setTimer(timer);
                clients[1].setTimer(timer);
            }
        }
    }
}
