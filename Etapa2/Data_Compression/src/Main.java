
import Arithmetic.*;
import Huffman.*;

import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {

        // Total number of tests in tests/in/ directory
        final int numberOfTests = 12;

        String inPath = "tests/in/test";
        String outPathA = "tests/out_1/Arithmetic/test";
        String outPathH = "tests/out_1/Huffman/test";
        //String outPathDecompressedA = "tests/out_de/Arithmetic/test";
        //String outPathDecompressedH = "tests/out_de/Huffman/test";

        // File to write execution times
        // Replace EXECUTION_TIMES_FILE with desired file name
        FileWriter fileWriterTime = new FileWriter("results/EXECUTION_TIMES_FILE.txt", true);

        // File to write memory used
        /*
         works only for one test and one algorithm at a time because
         program has to start from the beginning each time in order to reset memory usage
         */
        // Replace MEMORY_USAGE_FILE with desired file name
        FileWriter fileWriterMem = new FileWriter("results/MEMORY_USAGE_FILE.txt", true);


        PrintWriter printWriterTime = new PrintWriter(fileWriterTime);
        PrintWriter printWriterMem = new PrintWriter(fileWriterMem);

        // Iterate through test files
        for (int i = 1; i <= numberOfTests; i++) {

            printWriterTime.println("Compression times for test" + i + ":");
            printWriterMem.println("Memory used for compression of test" + i + ":");

            // ====== Compress all test files ======

            long startTimeA = System.nanoTime();

            ArithmeticCompress.execute(inPath, outPathA, i);

            printWriterTime.println("Arithmetic compression | "
                    + (double)(System.nanoTime() - startTimeA)/1000000000 + "s");
            printWriterMem.println("Arithmetic compression | "
                    + (double) (Runtime.getRuntime().totalMemory()
                    - Runtime.getRuntime().freeMemory()) / 1024 + " KB");

            // For test no 12 (~100 MB in size) compiler gives error "OutOfMemoryError"
            // when using Huffman compression
            /*
                Used to avoid error
            */
            if (i < 12) {
                long startTimeH = System.nanoTime();
                Huffman.compress(inPath + i + ".in", outPathH + i + ".out");

                printWriterTime.println("Huffman compression | "
                        + (double)(System.nanoTime() - startTimeH) / 1000000000 + "s" + "\n");
                printWriterMem.println("Huffman compression | "
                        + (double)(Runtime.getRuntime().totalMemory()
                        - Runtime.getRuntime().freeMemory()) / 1024 + " KB");
            }
            else {
                printWriterTime.println("Huffman compression | Out of memory error\n");
                printWriterMem.println("Huffman compression | Out of memory error\n");
            }

            // ====== Decompress previously compressed files (for verifying) ======

            /*

            ArithmeticDecompress.execute(outPathA, outPathDecompressedA, i);
            Huffman.decompress(outPathH + i + ".out", outPathDecompressedH + i + ".out");

            */
            // For usage uncomment outPathDecompressedA & outPathDecompressedH
        }
        printWriterTime.close();
        printWriterMem.close();
    }
}