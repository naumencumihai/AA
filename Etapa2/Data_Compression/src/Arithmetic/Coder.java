package Arithmetic;

import java.io.IOException;

public abstract class Coder {

    protected final int numStateBits;

    protected final long fullRange;

    protected final long halfRange;

    protected final long quarterRange;

    protected final long minimumRange;

    protected final long maximumTotal;

    protected final long stateMask;

    protected long low;

    protected long high;

    public Coder(int numBits) {
        if (numBits < 1 || numBits > 62)
            throw new IllegalArgumentException("State size out of range");
        numStateBits = numBits;
        fullRange = 1L << numStateBits;
        halfRange = fullRange >>> 1;  // Non-zero
        quarterRange = halfRange >>> 1;  // Can be zero
        minimumRange = quarterRange + 2;  // At least 2
        maximumTotal = Math.min(Long.MAX_VALUE / fullRange, minimumRange);
        stateMask = fullRange - 1;

        low = 0;
        high = stateMask;
    }

    protected void update(CheckedFreqTable freqs, int symbol) throws IOException {
        // State check
        if (low >= high || (low & stateMask) != low || (high & stateMask) != high)
            throw new AssertionError("Low or high out of range");
        long range = high - low + 1;
        if (range < minimumRange || range > fullRange)
            throw new AssertionError("Range out of range");

        // Frequency table values check
        long total = freqs.getTotal();
        long symLow = freqs.getLow(symbol);
        long symHigh = freqs.getHigh(symbol);
        if (symLow == symHigh)
            throw new IllegalArgumentException("Symbol has zero frequency");
        if (total > maximumTotal)
            throw new IllegalArgumentException("Cannot code symbol because total is too large");

        // Update range
        long newLow  = low + symLow  * range / total;
        long newHigh = low + symHigh * range / total - 1;
        low = newLow;
        high = newHigh;

        // While low and high have the same top bit value, shift them out
        while (((low ^ high) & halfRange) == 0) {
            shift();
            low  = ((low  << 1) & stateMask);
            high = ((high << 1) & stateMask) | 1;
        }
        // Now low's top bit must be 0 and high's top bit must be 1

        // While low's top two bits are 01 and high's are 10, delete the second highest bit of both
        while ((low & ~high & quarterRange) != 0) {
            underflow();
            low = (low << 1) ^ halfRange;
            high = ((high ^ halfRange) << 1) | halfRange | 1;
        }
    }

    protected abstract void shift() throws IOException;

    protected abstract void underflow() throws IOException;

}
