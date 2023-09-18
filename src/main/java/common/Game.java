package common;

import client.rmi.GameCallBackInterface;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.rmi.RemoteException;

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
    private Player[] players;
    private String[] chess;

    private GameCallBackInterface[] clients;

    public boolean isMyTurn(String username) {
        return players[turn].getUsername().equals(username);
    }

    public void move(int x, int y) throws RemoteException {
        board[x][y] = chess[turn].charAt(0);
        turn = 1 - turn;
        String turnLabel;
        if (!finished()) {
            turnLabel = getTurnLabel();
        } else {
            if (winner == -1) {
                turnLabel = "Match Drawn";
            } else {
                turnLabel = "Player " + players[winner].getUsername() + " wins!";
            }
        }
        clients[0].move(chess[1 - turn], x, y, turnLabel);
        clients[1].move(chess[1 - turn], x, y, turnLabel);
    }

    public String getTurnLabel() {
        return players[turn].toString() + "'s turn (" + chess[turn] + ")";
    }

    public boolean finished() {
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
}
