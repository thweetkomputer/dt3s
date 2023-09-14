package cmd;

/**
 * command line client starter.
 */
public class Client {
    public static void main(String[] args) {
        // use try-catch to avoid the program crash and give a friendly reminder.
        try {
            String username = args[0];
            String ip = args[1];
            int port = Integer.parseInt(args[2]);
//            new client.Client(username, ip, port).start();
        } catch (Exception e) {
            System.err.println("Usage: java -jar client.jar <username> <ip> <port>");
        }
    }
}
