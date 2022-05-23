package Arithmetic;

import java.util.Objects;

public final class SimpleFreqTable implements FreqTable {

    private int[] frequencies;
    private int[] cumulative;
    private int total;

    public SimpleFreqTable(int[] freqs) {
        Objects.requireNonNull(freqs);
        if (freqs.length < 1)
            throw new IllegalArgumentException("At least 1 symbol needed");
        if (freqs.length > Integer.MAX_VALUE - 1)
            throw new IllegalArgumentException("Too many symbols");

        frequencies = freqs.clone();  // Make copy
        total = 0;
        for (int x : frequencies) {
            if (x < 0)
                throw new IllegalArgumentException("Negative frequency");
            total = checkedAdd(x, total);
        }
        cumulative = null;
    }

    public SimpleFreqTable(FreqTable freqs) {
        Objects.requireNonNull(freqs);
        int numSym = freqs.getSymbolLimit();
        if (numSym < 1)
            throw new IllegalArgumentException("At least 1 symbol needed");

        frequencies = new int[numSym];
        total = 0;
        for (int i = 0; i < frequencies.length; i++) {
            int x = freqs.get(i);
            if (x < 0)
                throw new IllegalArgumentException("Negative frequency");
            frequencies[i] = x;
            total = checkedAdd(x, total);
        }
        cumulative = null;
    }

    public int getSymbolLimit() {
        return frequencies.length;
    }

    public int get(int symbol) {
        checkSymbol(symbol);
        return frequencies[symbol];
    }

    public void set(int symbol, int freq) {
        checkSymbol(symbol);
        if (freq < 0)
            throw new IllegalArgumentException("Negative frequency");

        int temp = total - frequencies[symbol];
        if (temp < 0)
            throw new AssertionError();
        total = checkedAdd(temp, freq);
        frequencies[symbol] = freq;
        cumulative = null;
    }

    public void increment(int symbol) {
        checkSymbol(symbol);
        if (frequencies[symbol] == Integer.MAX_VALUE)
            throw new ArithmeticException("Arithmetic overflow");
        total = checkedAdd(total, 1);
        frequencies[symbol]++;
        cumulative = null;
    }

    public int getTotal() {
        return total;
    }

    public int getLow(int symbol) {
        checkSymbol(symbol);
        if (cumulative == null)
            initCumulative();
        return cumulative[symbol];
    }

    public int getHigh(int symbol) {
        checkSymbol(symbol);
        if (cumulative == null)
            initCumulative();
        return cumulative[symbol + 1];
    }


    // Recomputes the array of cumulative symbol frequencies.
    private void initCumulative() {
        cumulative = new int[frequencies.length + 1];
        int sum = 0;
        for (int i = 0; i < frequencies.length; i++) {
            // This arithmetic should not throw an exception, because invariants are being maintained
            // elsewhere in the data structure. This implementation is just a defensive measure.
            sum = checkedAdd(frequencies[i], sum);
            cumulative[i + 1] = sum;
        }
        if (sum != total)
            throw new AssertionError();
    }


    // Returns silently if 0 <= symbol < frequencies.length, otherwise throws an exception.
    private void checkSymbol(int symbol) {
        if (symbol < 0 || symbol >= frequencies.length)
            throw new IllegalArgumentException("Symbol out of range");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < frequencies.length; i++)
            sb.append(String.format("%d\t%d%n", i, frequencies[i]));
        return sb.toString();
    }


    // Adds the given integers, or throws an exception if the result cannot be represented as an int (i.e. overflow).
    private static int checkedAdd(int x, int y) {
        int z = x + y;
        if (y > 0 && z < x || y < 0 && z > x)
            throw new ArithmeticException("Arithmetic overflow");
        else
            return z;
    }

}
