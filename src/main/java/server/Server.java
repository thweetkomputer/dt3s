package server;

import lombok.AllArgsConstructor;

/**
 * the server class.
 */
@AllArgsConstructor
public class Server {
    private String ip;
    private int port;

    public void start() {
        System.out.println("Server started.");
    }
}
