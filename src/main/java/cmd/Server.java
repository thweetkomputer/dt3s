package cmd;

/**
 * command line server starter.
 */
public class Server {
    public static void main(String[] args) {
        // use try-catch to avoid the program crash and give a friendly reminder.
        try {
            String ip = args[0];
            int port = Integer.parseInt(args[1]);
            new server.Server(ip, port).start();
        } catch (Exception e) {
            System.err.println("Usage: java -jar server.jar <ip> <port>");
        }
    }
}
