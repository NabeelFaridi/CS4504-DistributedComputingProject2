import java.net.*;
import java.io.*;
import java.util.Arrays;

public class TCPServerRouter {
    public static void main(String[] args) throws IOException {
        Socket clientSocket = null; // socket for the thread
        Object[][] RoutingTable = new Object[10][2]; // routing table

        int ind = 0; // index in the routing table
        int port = 5555; // port number
        boolean running = true;

        // Accepting connections
        ServerSocket serverSocket = null; // server socket for accepting connections
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("ServerRouter is Listening on port: " + port);
        } catch (IOException e) {
            System.err.println(e);
            System.err.println("Could not listen on port: 5555.");
            System.exit(1);
        }

        // Creating threads with accepted connections
        while (running) {
            try {
                clientSocket = serverSocket.accept();

                // Get the port number from the client, so that we can run a client and server on the same IP
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                int clientPort = Integer.parseInt(in.readLine());

                // creates a thread
                SThread t = new SThread(RoutingTable, clientSocket, ind, clientPort);
                t.start();
                ind++;

                System.out.println("ServerRouter connected with Client/Server: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientPort);
            } catch (IOException e) {
                System.err.println("Client/Server failed to connect.");
                System.exit(1);
            }
        }

        clientSocket.close();
        serverSocket.close();
    }
}