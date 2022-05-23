package Arithmetic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ArithmeticDecompress {

    public static void execute(String inPath, String outPath, int testNumber) throws IOException {
        File inputFile  = new File(inPath + testNumber + ".out");
        File outputFile = new File(outPath + testNumber + ".out");

        // Perform file decompression
        try (BitsIn in = new BitsIn(new BufferedInputStream(new FileInputStream(inputFile)));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            FreqTable freqs = readFrequencies(in);
            decompress(freqs, in, out);
        }
    }

    private static FreqTable readFrequencies(BitsIn in) throws IOException {
        int[] freqs = new int[257];
        for (int i = 0; i < 256; i++)
            freqs[i] = readInt(in, 32);
        freqs[256] = 1;  // EOF symbol
        return new SimpleFreqTable(freqs);
    }


   private  static void decompress(FreqTable freqs, BitsIn in, OutputStream out) throws IOException {
        Decoder dec = new Decoder(32, in);
        while (true) {
            int symbol = dec.read(freqs);
            if (symbol == 256)  // EOF symbol
                break;
            out.write(symbol);
        }
    }


    // Reads an unsigned integer of the given bit width from the given stream.
    private static int readInt(BitsIn in, int numBits) throws IOException {
        if (numBits < 0 || numBits > 32)
            throw new IllegalArgumentException();

        int result = 0;
        for (int i = 0; i < numBits; i++)
            result = (result << 1) | in.readNoEof();  // Big endian
        return result;
    }

}
