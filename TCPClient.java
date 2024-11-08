import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

public class TCPClient {

    // Server configuration
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 6789;

    // Method to generate a random matrix of size N x N
    public static int[][] generateMatrix(int size) {
        Random random = new Random();
        int[][] matrix = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = random.nextInt(100); // Random integers between 0 and 99
            }
        }

        return matrix;
    }

    // Method to send two matrices and receive the result from the server
    public void sendMatricesAndReceiveResult(int[][] matrixA, int[][] matrixB) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Send the first matrix
            out.writeObject(matrixA);
            out.flush();
            System.out.println("CLIENT: First matrix sent to the server.");

            // Send the second matrix
            out.writeObject(matrixB);
            out.flush();
            System.out.println("CLIENT: Second matrix sent to the server.");

            // Receive the result matrix from the server
            int[][] resultMatrix = (int[][]) in.readObject();
            System.out.println("CLIENT: Result matrix received from the server:");

            // Print the result matrix for verification
            printMatrix(resultMatrix);

        } catch (Exception e) {
            System.err.println("CLIENT: Error communicating with the server: " + e.getMessage());
        }
    }

    // Helper method to print the matrix
    private void printMatrix(int[][] matrix) {
        System.out.println("CLIENT");
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }

    // Main method for testing the client setup
    public static void main(String[] args) {
        TCPClient client = new TCPClient();
        int size = 128;
        int[][] matrixA = TCPClient.generateMatrix(size); // Generate first 1K x 1K matrix
        int[][] matrixB = TCPClient.generateMatrix(size); // Generate second 1K x 1K matrix
        client.sendMatricesAndReceiveResult(matrixA, matrixB);
    }
}
