package Arithmetic;

import java.io.IOException;
import java.util.Objects;

public final class Decoder extends Coder {

    private BitsIn input;
    private long code;

    public Decoder(int numBits, BitsIn in) throws IOException {
        super(numBits);
        input = Objects.requireNonNull(in);
        code = 0;
        for (int i = 0; i < numStateBits; i++)
            code = code << 1 | readCodeBit();
    }

    public int read(FreqTable freqs) throws IOException {
        return read(new CheckedFreqTable(freqs));
    }

    public int read(CheckedFreqTable freqs) throws IOException {
        // Translate from coding range scale to frequency table scale
        long total = freqs.getTotal();
        if (total > maximumTotal)
            throw new IllegalArgumentException("Cannot decode symbol because total is too large");
        long range = high - low + 1;
        long offset = code - low;
        long value = ((offset + 1) * total - 1) / range;
        if (value * range / total > offset)
            throw new AssertionError();
        if (value < 0 || value >= total)
            throw new AssertionError();

        int start = 0;
        int end = freqs.getSymbolLimit();
        while (end - start > 1) {
            int middle = (start + end) >>> 1;
            if (freqs.getLow(middle) > value)
                end = middle;
            else
                start = middle;
        }
        if (start + 1 != end)
            throw new AssertionError();

        int symbol = start;
        if (offset < freqs.getLow(symbol) * range / total || freqs.getHigh(symbol) * range / total <= offset)
            throw new AssertionError();
        update(freqs, symbol);
        if (code < low || code > high)
            throw new AssertionError("Code out of range");
        return symbol;
    }

    protected void shift() throws IOException {
        code = ((code << 1) & stateMask) | readCodeBit();
    }

    protected void underflow() throws IOException {
        code = (code & halfRange) | ((code << 1) & (stateMask >>> 1)) | readCodeBit();
    }

    // Returns the next bit (0 or 1) from the input stream. The end
    // of stream is treated as an infinite number of trailing zeros.
    private int readCodeBit() throws IOException {
        int temp = input.read();
        if (temp == -1)
            temp = 0;
        return temp;
    }

}
