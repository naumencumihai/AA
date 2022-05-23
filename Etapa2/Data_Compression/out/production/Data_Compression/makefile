JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	ArithmeticCompress.java\
	src\Arithmetic\ArithmeticDecompress.java\
	src\Arithmetic\BItsIn.java\
	src\Arithmetic\BitsOut.java\
	src\Arithmetic\CheckedFreqTable.java\
	src\Arithmetic\Coder.java\
	src\Arithmetic\Decoder.java\
	src\Arithmetic\Encoder.java\
	src\Arithmetic\FreqTable.java\
	src\Arithmetic\SimpleFreqTable.java\
	src\Huffman\BinaryToFile.java\
	src\Huffman\CharIterator.java\
	src\Huffman\Huffman.java\
	src\Main.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
run:
	java Main
