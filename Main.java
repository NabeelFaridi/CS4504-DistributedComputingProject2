import java.io.IOException;

public class Main {
    static String[] clientIps = {
        "127.0.0.1:1234",
        "127.0.0.1:2345",
        "127.0.0.1:3456",
        "127.0.0.1:4567",
    };

    static String[] serverIps = {
        "127.0.0.1:5678",
        "127.0.0.1:6789",
        "127.0.0.1:7890",
        "127.0.0.1:8901",
    };

    public static void main(String[] args) throws IOException {
        Thread serverThread = new Thread(() ->{
            try{
                TCPServer.main(args);
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        serverThread.start();
        Thread clientThread = new Thread(() -> {
            try {
                TCPClient.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        clientThread.start();
    }

    @SuppressWarnings("unused")
    private static void part1(String[] args){
        // Start the router
        Thread routerThread = new Thread(() -> {
            try {
                TCPServerRouter.main(args);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });        

        routerThread.start();

        if (serverIps.length != clientIps.length) {
            System.out.println("The number of clients and servers must be the same.");
            System.exit(1);
        }

        for (int i = 0; i < clientIps.length; i++) {
            // Create a new client, and pass it the server it should connect to
            String[] clientArgs = {serverIps[i], clientIps[i]};
            Thread clientThread = new Thread(() -> {
                try {
                    TCPClient.main(clientArgs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            clientThread.start();

            // Wait 1 second for the client to connect
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Connect the server thread to the router
            String[] serverArgs = {clientIps[i], serverIps[i]};
            Thread serverThread = new Thread(() -> {
                try {
                    TCPServer.main(serverArgs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


            serverThread.start();

            // Wait 1 second for the server to connect
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
