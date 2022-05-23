package Arithmetic;

import java.util.Objects;

public final class CheckedFreqTable implements FreqTable {

    private FreqTable freqTable;
    public CheckedFreqTable(FreqTable freq) {
        freqTable = Objects.requireNonNull(freq);
    }

    public int getSymbolLimit() {
        int result = freqTable.getSymbolLimit();
        if (result <= 0)
            throw new AssertionError("Non-positive symbol limit");
        return result;
    }

    public int get(int symbol) {
        int result = freqTable.get(symbol);
        if (!isSymbolInRange(symbol))
            throw new AssertionError("IllegalArgumentException expected");
        if (result < 0)
            throw new AssertionError("Negative symbol frequency");
        return result;
    }

    public int getTotal() {
        int result = freqTable.getTotal();
        if (result < 0)
            throw new AssertionError("Negative total frequency");
        return result;
    }

    public int getLow(int symbol) {
        if (isSymbolInRange(symbol)) {
            int low   = freqTable.getLow (symbol);
            int high  = freqTable.getHigh(symbol);
            if (!(0 <= low && low <= high && high <= freqTable.getTotal()))
                throw new AssertionError("Symbol low cumulative frequency out of range");
            return low;
        } else {
            freqTable.getLow(symbol);
            throw new AssertionError("IllegalArgumentException expected");
        }
    }

    public int getHigh(int symbol) {
        if (isSymbolInRange(symbol)) {
            int low   = freqTable.getLow (symbol);
            int high  = freqTable.getHigh(symbol);
            if (!(0 <= low && low <= high && high <= freqTable.getTotal()))
                throw new AssertionError("Symbol high cumulative frequency out of range");
            return high;
        } else {
            freqTable.getHigh(symbol);
            throw new AssertionError("IllegalArgumentException expected");
        }
    }

    public String toString() {
        return "Arithmetic.CheckedFreqTable (" + freqTable.toString() + ")";
    }

    public void set(int symbol, int freq) {
        freqTable.set(symbol, freq);
        if (!isSymbolInRange(symbol) || freq < 0)
            throw new AssertionError("IllegalArgumentException expected");
    }

    public void increment(int symbol) {
        freqTable.increment(symbol);
        if (!isSymbolInRange(symbol))
            throw new AssertionError("IllegalArgumentException expected");
    }

    private boolean isSymbolInRange(int symbol) {
        return 0 <= symbol && symbol < getSymbolLimit();
    }

}
