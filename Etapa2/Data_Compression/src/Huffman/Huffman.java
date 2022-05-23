package Huffman;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class Huffman {
    // Huffman tree node
    private static class TreeNode {
        private final TreeNode myLeft;
        private final TreeNode myRight;
        private final String character;
        private final int frequency;

        public TreeNode(TreeNode left, TreeNode right, String ch, int freq) {
            myLeft = left;
            myRight = right;
            character = ch;
            frequency = freq;
        }

        // Checks if a certain treenode is a leaf
        public boolean isLeaf() {
            return myLeft == null;
        }
    }

    // Hashmap object that stores the frequency of each letter character in the file.
    public static HashMap<String, Integer> frequencyCount(Iterator<String> iterator) {
        HashMap<String, Integer> binaryFrequency = new HashMap<>();

        while (iterator.hasNext()) {
            String binary = iterator.next();

            if (binaryFrequency.containsKey(binary))
                binaryFrequency.put(binary, binaryFrequency.get(binary) + 1);
            else
                binaryFrequency.put(binary, 1);
        }
        return binaryFrequency;
    }


    // Looks for the characters with the minimum frequency
    // and then removes them from the hashmap
    public static Map.Entry<String, Integer> delMinFrequency(HashMap<String, Integer> binaryFrequency) {
        Integer min = Collections.min(binaryFrequency.values());

        for (Map.Entry<String, Integer> entry : binaryFrequency.entrySet()) {
            if (entry.getValue().equals(min)) {
                binaryFrequency.remove(entry.getKey());
                return entry;
            }
        }
        return null;
    }

    // Comparator TreeNode class implementation
    public static Comparator<TreeNode> idComparator = Comparator.comparingInt(c -> c.frequency);

    // Builds the Huffman trie according to the frequencies
    public static HashMap<String, String> buildTrie(HashMap<String, Integer> binaryFrequency) {
        PriorityQueue<TreeNode> leafNode = new PriorityQueue<>(binaryFrequency.size(), idComparator);

        // Creates the leafs
        while (binaryFrequency.size() > 0) {
            Map.Entry<String, Integer> leaf = delMinFrequency(binaryFrequency);
            assert leaf != null;
            leafNode.add(new TreeNode(null, null, leaf.getKey(), leaf.getValue()));
        }

        // Merges the leafs
        while (leafNode.size() > 1) {
            TreeNode left = leafNode.remove();
            TreeNode right = leafNode.remove();
            leafNode.add(new TreeNode(left, right, null, left.frequency + right.frequency));
        }

        // Construct a table mapping characters
        assert leafNode.peek() != null;
        return codewords(new HashMap<>(), leafNode.peek(), "");
    }


//  ============== Compress ==============

    // Creates a hashmap of Compressed codewords
    public static HashMap<String, String> codewords(HashMap<String, String> cws, TreeNode x, String s) {
        if (x.isLeaf())
            cws.put(x.character, s);
        else {
            codewords(cws, x.myLeft, s + "0");
            codewords(cws, x.myRight, s + "1");
        }
        return cws;
    }


    // Creates a header at the beginning of the file.
    public static String formatHeader(HashMap<String, String> codewords) {
        StringBuilder header = new StringBuilder();

        for (Map.Entry<String, String> entry : codewords.entrySet())
            header.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        return header + "\n";
    }

    // Converts the Compressed binary strings into a format of 8 bits each.
    public static String convertTo8bits(String binary) {
        return "0".repeat(Math.max(0, (8 - binary.length()))) + binary;
    }


    // Displays the header in file format.
    public static void writeHeader(String header, String outputFileName) {
        for (int i = 0; i < header.length(); i++) {
            String section = convertTo8bits(Integer.toBinaryString(header.charAt(i)));
            BinaryToFile.write(section, outputFileName);
        }
    }

    // Encoding each individual character.
    public static StringBuilder  encodeCharacters(HashMap<String, String> codes, Iterator<String> iterator) {
        StringBuilder binChar = new StringBuilder();

        while (iterator.hasNext())
            binChar.append(codes.get(iterator.next()));
        return binChar.append(codes.get("EOF"));
    }

    // Creates the body of the Compressed file.
    public static void writeBody(StringBuilder binChar, String outputFileName) {
        // write the bytes to the file
        while (binChar.length() > 8) {
            int readMax = 8 * (binChar.length() / 8);
            BinaryToFile.write(binChar.substring(0, readMax), outputFileName);
            binChar.delete(0, readMax);
        }

        if (binChar.length() != 0) {
            binChar.append("0".repeat(Math.max(0, 8 - binChar.length())));
            BinaryToFile.write(binChar.toString(), outputFileName);
        }
    }

    // Loads the Compressed file.
    public static Queue<String> loadFile(Iterator<String> it) {
        Queue<String> bin = new LinkedList<>();

        while (it.hasNext())
            bin.add(it.next());
        return bin;
    }

    // Tries to delete a non-existing file
    public static void deleteIfExists(String path) {
        File f = new File(path);

        try {
            f.delete();
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Encode method
    public static void compress(String target, String destination) {
        Queue<String> file = loadFile(new CharIterator(target));
        HashMap<String, Integer> binaryFrequency = frequencyCount(file.iterator());
        binaryFrequency.put("EOF", 1);
        HashMap<String, String> codes = buildTrie(binaryFrequency);
        String header = formatHeader(codes);
        StringBuilder binChar = encodeCharacters(codes, file.iterator());

        deleteIfExists(destination);
        writeHeader(header, destination);
        writeBody(binChar, destination);
    }

//  ============== Decoding ==============

    // Returns codewords in the Compressed file.
    public static HashMap<String, String> retrieveCodewords(String header) {
        String[] lines = header.split("\n");
        HashMap<String, String> codewords = new HashMap<>();

        for (String line : lines) {
            String[] codes = line.split(",");

            if (codes.length > 1)
                codewords.put(codes[1], codes[0]);
        }
        return codewords;
    }


    // Returns the Compressed header from a file
    public static String retrieveCompressedHeader(CharIterator it) {
        String endOfHeader = convertTo8bits(Integer.toBinaryString('\n'));
        StringBuilder header = new StringBuilder();
        StringBuilder doubleKey = new StringBuilder();
        String section;
        boolean error = true;

        endOfHeader += endOfHeader;
        while (it.hasNext()) {
            section = it.next();
            doubleKey.append(section);
            header.append((char) Integer.parseInt(section, 2));

            if (doubleKey.length() == 16) {
                if (doubleKey.toString().equals(endOfHeader)) {
                    error = false;
                    break;
                }
                doubleKey = new StringBuilder(doubleKey.substring(8));
            }
        }

        // Checks if the header is well formated
        if (error) {
            System.err.println("Error: bad header format");
            System.exit(0);
        }
        return header.toString();
    }

    // Returns the Compressed body from a file
    public static String retrieveCompressedBody(CharIterator it) {
        StringBuilder section = new StringBuilder();

        while (it.hasNext())
            section.append(it.next());
        return section.toString();
    }

    // Searches in the codewords hashmap the new compresed binary
    public static String searchForCode(String code, HashMap<String, String> codewords) {
        return codewords.getOrDefault(code, null);
    }

    // Writes the Decompressed file
    public static void writeDecompressedFile(StringBuilder section, String destination) {
        while (section.length() > 8) {
            int readMax = 8 * (section.length() / 8);
            BinaryToFile.write(section.substring(0, readMax), destination);
            section.delete(0, readMax);
        }

        if (section.length() != 0) {
            section.append("0".repeat(Math.max(0, 8 - section.length())));
            BinaryToFile.write(section.toString(), destination);
        }
    }

    // Decode method
    public static void decompress(String target, String destination) {
        CharIterator it = new CharIterator(target);
        String header = retrieveCompressedHeader(it);
        String body = retrieveCompressedBody(it);
        HashMap<String, String> codewords = retrieveCodewords(header);
        String bin;
        int i = 0;
        StringBuilder section = new StringBuilder();

        while (body.length() > 1) {
            bin = body.substring(0, i);
            String code = searchForCode(bin, codewords);

            if (code != null) {
                if (code.equals("EOF"))
                    break;
                section.append(code);
                body = body.substring(i);
                i = 0;
            }
            i++;
        }

        deleteIfExists(destination);
        writeDecompressedFile(section, destination);
    }
}
