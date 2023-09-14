package client;

import exception.LoginException;
import lombok.AllArgsConstructor;
import rmi.LoginInterface;

/**
 * the client class.
 */
@AllArgsConstructor
public class Client {
    private String username;
    private String ip;
    private int port;

    public void start() throws Exception {
        System.out.println("Client started.");
        LoginInterface loginInterface = (LoginInterface) java.rmi.Naming.lookup("rmi://" + ip + ":" + port + "/login");
        String result = loginInterface.Login(username);
        if (!result.equals("OK")) {
            throw new LoginException(result);
        }
    }
}
