import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StrassenMatrixMultiplierWithMetrics {

    private double parallelExecutionTime;
    private double sequentialExecutionTime;

    public int[][] multiplyWithMetrics(int[][] A, int[][] B) throws Exception {
        // Measure parallel execution time
        long startTimeParallel = System.nanoTime();
        int[][] parallelResult = parallelMultiply(A, B);
        long endTimeParallel = System.nanoTime();
        parallelExecutionTime = (endTimeParallel - startTimeParallel) / 1e6; // in milliseconds

        // Measure sequential execution time
        long startTimeSequential = System.nanoTime();
        int[][] sequentialResult = sequentialMultiply(A, B);
        long endTimeSequential = System.nanoTime();
        sequentialExecutionTime = (endTimeSequential - startTimeSequential) / 1e6; // in milliseconds

        // Calculate speedup and efficiency
        calculateAndDisplayMetrics();

        return parallelResult;
    }

    private void calculateAndDisplayMetrics() {
        double speedUp = sequentialExecutionTime / parallelExecutionTime;
        int numThreads = 7; // Number of threads used in parallel multiplication
        double efficiency = speedUp / numThreads;

        System.out.println("Performance Metrics:");
        System.out.println("Parallel Execution Time: " + parallelExecutionTime + " ms");
        System.out.println("Sequential Execution Time: " + sequentialExecutionTime + " ms");
        System.out.println("Speed Up: " + speedUp);
        System.out.println("Efficiency: " + efficiency);
    }

    private int[][] parallelMultiply(int[][] A, int[][] B) throws Exception {
        return multiply(A, B);
    }

    // Define the Strassen's algorithm multiply method here
    private int[][] multiply(int[][] A, int[][] B) throws Exception {
        int n = A.length;

        // Base case for recursion
        if (n == 1) {
            int[][] C = { { A[0][0] * B[0][0] } };
            return C;
        }

        // Initialize submatrices
        int newSize = n / 2;
        int[][] a11 = new int[newSize][newSize];
        int[][] a12 = new int[newSize][newSize];
        int[][] a21 = new int[newSize][newSize];
        int[][] a22 = new int[newSize][newSize];
        int[][] b11 = new int[newSize][newSize];
        int[][] b12 = new int[newSize][newSize];
        int[][] b21 = new int[newSize][newSize];
        int[][] b22 = new int[newSize][newSize];

        // Split matrices A and B into submatrices
        split(A, a11, 0, 0);
        split(A, a12, 0, newSize);
        split(A, a21, newSize, 0);
        split(A, a22, newSize, newSize);
        split(B, b11, 0, 0);
        split(B, b12, 0, newSize);
        split(B, b21, newSize, 0);
        split(B, b22, newSize, newSize);

        // Use ExecutorService to manage threads for the 7 products
        ExecutorService executor = Executors.newFixedThreadPool(7);

        Future<int[][]> m1 = executor.submit(() -> multiply(add(a11, a22), add(b11, b22)));
        Future<int[][]> m2 = executor.submit(() -> multiply(add(a21, a22), b11));
        Future<int[][]> m3 = executor.submit(() -> multiply(a11, subtract(b12, b22)));
        Future<int[][]> m4 = executor.submit(() -> multiply(a22, subtract(b21, b11)));
        Future<int[][]> m5 = executor.submit(() -> multiply(add(a11, a12), b22));
        Future<int[][]> m6 = executor.submit(() -> multiply(subtract(a21, a11), add(b11, b12)));
        Future<int[][]> m7 = executor.submit(() -> multiply(subtract(a12, a22), add(b21, b22)));

        // Combine results to form submatrices of C
        int[][] c11 = add(subtract(add(m1.get(), m4.get()), m5.get()), m7.get());
        int[][] c12 = add(m3.get(), m5.get());
        int[][] c21 = add(m2.get(), m4.get());
        int[][] c22 = add(subtract(add(m1.get(), m3.get()), m2.get()), m6.get());

        executor.shutdown();

        // Join results into a single matrix C
        int[][] C = new int[n][n];
        join(c11, C, 0, 0);
        join(c12, C, 0, newSize);
        join(c21, C, newSize, 0);
        join(c22, C, newSize, newSize);

        return C;
    }

    private int[][] sequentialMultiply(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = 0;
                for (int k = 0; k < n; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return C;
    }

    private int[][] add(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] + B[i][j];
            }
        }
        return C;
    }

    private int[][] subtract(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] - B[i][j];
            }
        }
        return C;
    }

    private void split(int[][] parent, int[][] child, int iB, int jB) {
        for (int i = 0; i < child.length; i++) {
            for (int j = 0; j < child.length; j++) {
                child[i][j] = parent[i + iB][j + jB];
            }
        }
    }

    private void join(int[][] child, int[][] parent, int iB, int jB) {
        for (int i = 0; i < child.length; i++) {
            for (int j = 0; j < child.length; j++) {
                parent[i + iB][j + jB] = child[i][j];
            }
        }
    }
}
