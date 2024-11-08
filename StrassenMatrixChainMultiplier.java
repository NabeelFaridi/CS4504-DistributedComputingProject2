import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StrassenMatrixChainMultiplier {

    private StrassenMatrixMultiplierWithMetrics multiplier;
    private ExecutorService executor;
    private int maxRecursionDepth;

    public StrassenMatrixChainMultiplier(int numMatrices, int maxDepth) {
        int threadCount = calculateThreadCount(numMatrices);
        this.executor = Executors.newFixedThreadPool(threadCount);
        this.multiplier = new StrassenMatrixMultiplierWithMetrics();
        this.maxRecursionDepth = maxDepth;
        System.out.println("Initialized with " + threadCount + " threads and max recursion depth of " + maxDepth + " for " + numMatrices + " matrices.");
    }

    public int[][] multiplyChain(int[][][] matrices) throws Exception {
        if (matrices.length == 1) {
            return matrices[0]; // Only one matrix, nothing to multiply
        }

        return multiplyChainRecursive(matrices, 0, matrices.length - 1, 0); // Initial depth is 0
    }

    private int[][] multiplyChainRecursive(int[][][] matrices, int start, int end, int depth) throws Exception {
        if (start == end) {
            return matrices[start];
        }

        if (depth >= maxRecursionDepth) {
            System.out.println("Reached max recursion depth; switching to sequential multiplication.");
            return sequentialMultiplyChain(matrices, start, end);
        }

        int mid = (start + end) / 2;

        Future<int[][]> leftResult = executor.submit(() -> multiplyChainRecursive(matrices, start, mid, depth + 1));
        Future<int[][]> rightResult = executor.submit(() -> multiplyChainRecursive(matrices, mid + 1, end, depth + 1));

        int[][] leftMatrix = leftResult.get();
        int[][] rightMatrix = rightResult.get();

        return multiplier.multiplyWithMetrics(leftMatrix, rightMatrix);
    }

    private int[][] sequentialMultiplyChain(int[][][] matrices, int start, int end) throws Exception {
        int[][] result = matrices[start];
        for (int i = start + 1; i <= end; i++) {
            result = multiplier.multiplyWithMetrics(result, matrices[i]);
        }
        return result;
    }

    private int calculateThreadCount(int numMatrices) {
        if (numMatrices == 2) return 1;
        else if (numMatrices == 4) return 3;
        else if (numMatrices == 8) return 7;
        else if (numMatrices == 16) return 15;
        else if (numMatrices == 32) return 31;
        else return Math.min(31, numMatrices / 2); // For non-standard cases, ensure max 31 threads
    }

    public void shutdown() {
        executor.shutdown();
    }

    public static void main(String[] args) throws Exception {
        int size = 1024; // Define a larger matrix size to test memory and execution limits
        int[][] matrixA = generateMatrix(size);
        int[][] matrixB = generateMatrix(size);
        int[][] matrixC = generateMatrix(size);
        int[][] matrixD = generateMatrix(size);

        int[][][] matrices = { matrixA, matrixB, matrixC, matrixD };

        StrassenMatrixChainMultiplier chainMultiplier = new StrassenMatrixChainMultiplier(matrices.length, 4); // Set max recursion depth to 4
        int[][] result = chainMultiplier.multiplyChain(matrices);

        System.out.println("Final Result Matrix:");
        printMatrix(result);

        chainMultiplier.shutdown();
    }

    private static int[][] generateMatrix(int size) {
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = (int) (Math.random() * 100);
            }
        }
        return matrix;
    }

    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }
}
