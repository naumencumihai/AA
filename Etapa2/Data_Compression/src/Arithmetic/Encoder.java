package Arithmetic;

import java.io.IOException;
import java.util.Objects;

public final class Encoder extends Coder {

    private BitsOut output;
    private int numUnderflow;

    public Encoder(int numBits, BitsOut out) {
        super(numBits);
        output = Objects.requireNonNull(out);
        numUnderflow = 0;
    }

    public void write(FreqTable freqs, int symbol) throws IOException {
        write(new CheckedFreqTable(freqs), symbol);
    }

    public void write(CheckedFreqTable freqs, int symbol) throws IOException {
        update(freqs, symbol);
    }

    public void finish() throws IOException {
        output.write(1);
    }

    protected void shift() throws IOException {
        int bit = (int)(low >>> (numStateBits - 1));
        output.write(bit);

        // Write out the saved underflow bits
        for (; numUnderflow > 0; numUnderflow--)
            output.write(bit ^ 1);
    }

    protected void underflow() {
        if (numUnderflow == Integer.MAX_VALUE)
            throw new ArithmeticException("Maximum underflow reached");
        numUnderflow++;
    }

}
