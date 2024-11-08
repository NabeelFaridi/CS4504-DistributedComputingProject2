import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    // Server configuration
    private static final int SERVER_PORT = 6789;

    public static void main(String[] args) {
        new TCPServer().startServer();
    }

    // Method to start the server and listen for client connections
    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server is listening on port " + SERVER_PORT);

            // Continuously accept client connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                // Handle client connection in a new thread
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // Inner class to handle client connections
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

                // Receive two matrices from the client
                int[][] matrixA = (int[][]) in.readObject();
                System.out.println("Received first matrix from client.");
                int[][] matrixB = (int[][]) in.readObject();
                System.out.println("Received second matrix from client.");

                // Perform matrix multiplication with metrics
                System.out.println("Starting matrix multiplication with metrics...");
                StrassenMatrixMultiplierWithMetrics multiplier = new StrassenMatrixMultiplierWithMetrics();
                int[][] resultMatrix = multiplier.multiplyWithMetrics(matrixA, matrixB);
                System.out.println("Matrix multiplication completed with metrics.");

                // Send the result back to the client
                out.writeObject(resultMatrix);
                out.flush();
                System.out.println("Result matrix sent back to client.");

            } catch (IOException e) {
                System.err.println("Error handling client I/O: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.err.println("Error handling client data: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error during matrix multiplication: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("Client connection closed.");
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
}
