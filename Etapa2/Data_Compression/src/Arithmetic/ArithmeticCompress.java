package Arithmetic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ArithmeticCompress {

    public static void execute(String inPath, String outPath, int testNumber) throws IOException {
        File inputFile  = new File(inPath + testNumber + ".in");
        File outputFile = new File(outPath + testNumber + ".out");

        // Read input file once to compute symbol frequencies
        FreqTable freqs = getFrequencies(inputFile);
        freqs.increment(256);  // EOF symbol gets a frequency of 1

        // Read input file again, compress with arithmetic coding, and write output file
        try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
             BitsOut out = new BitsOut(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
            writeFrequencies(out, freqs);
            compress(freqs, in, out);
        }
    }


    // Returns a frequency table based on the bytes in the given file.
    private static FreqTable getFrequencies(File file) throws IOException {
        FreqTable freqs = new SimpleFreqTable(new int[257]);
        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            while (true) {
                int b = input.read();
                if (b == -1)
                    break;
                freqs.increment(b);
            }
        }
        return freqs;
    }

    private static void writeFrequencies(BitsOut out, FreqTable freqs) throws IOException {
        for (int i = 0; i < 256; i++)
            writeInt(out, 32, freqs.get(i));
    }

    private static void compress(FreqTable freqs, InputStream in, BitsOut out) throws IOException {
        Encoder enc = new Encoder(32, out);
        while (true) {
            int symbol = in.read();
            if (symbol == -1)
                break;
            enc.write(freqs, symbol);
        }
        enc.write(freqs, 256);  // EOF
        enc.finish();  // Flush remaining code bits
    }

    private static void writeInt(BitsOut out, int numBits, int value) throws IOException {
        if (numBits < 0 || numBits > 32)
            throw new IllegalArgumentException();

        for (int i = numBits - 1; i >= 0; i--)
            out.write((value >>> i) & 1);
    }

}
