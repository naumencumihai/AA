package Arithmetic;

public interface FreqTable {

    public int getSymbolLimit();

    public int get(int symbol);

    public void set(int symbol, int freq);

    public void increment(int symbol);

    public int getTotal();

    public int getLow(int symbol);

    public int getHigh(int symbol);

}
