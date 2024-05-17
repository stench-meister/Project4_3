/* Authors: Qaturi Vaughn & Moses Tennent
 * Assignment 4, TCSS 342 Spring 2024
 * This is the LZW class for the compressed literature 2 assignment
 * 
 * The LZW class implements the Lempel-Ziv-Welch compression algorithm to compress a 
 * given file or message. It replaces repeated occurrences of data with references 
 * to a single instance, compresses the file/message using the codes, and then decompresses.
 */

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

// no full package imports allowed, just class imports

// this version can't run the testLZW method in main (somethings 
// wrong with the decompress method), compression is kinda wack too
public class LZW {
    public static final int RESTART = 256;
    private static final int INITIAL_CODE_LENGTH = 9;
    private static final int MAX_CODE_LENGTH = 16;

    private int maxEvictions;
    private int totalEvictions;
    private int numberOfCompressions;

    public LZW() {
        maxEvictions = 0;
        totalEvictions = 0;
        numberOfCompressions = 0;
    }

    public byte[] compress(String fullText) {
        // Initialize the dictionary with all 1-byte strings plus the RESTART code
        MyCuckooTable<String, Integer> dictionary = new MyCuckooTable<>();
        for (int i = 0; i < RESTART; i++) {
            dictionary.put("" + (char) i, i);
        }
        dictionary.put( "",RESTART);
        List<Integer> compressedList = new ArrayList<>();
        int codeLength = INITIAL_CODE_LENGTH;
        int nextCode = RESTART + 1;
        String current = "";
        int evictions = 0;
        boolean reset;
        int cnt = 0;
        System.out.println("starting compression");
        for (char c : fullText.toCharArray()) {
            cnt++;
            String currentPlusC = current + c;
            if (dictionary.get(currentPlusC) != null) {
                current = currentPlusC;
                System.out.println("added " + currentPlusC + ", no evictions");
            } else {
                // Eviction occurs
                if (dictionary.get(current) != null) {
                    compressedList.add(dictionary.get(current));
                    System.out.println("added " + currentPlusC + ", with evictions");
                }

                reset = dictionary.put(currentPlusC, nextCode++);
            if (dictionary.size() >= (1 << codeLength) && codeLength < MAX_CODE_LENGTH) {
                    codeLength++;
                    evictions += dictionary.getEvictions();
                    totalEvictions += dictionary.getEvictions();
                    compressedList.add(dictionary.get(""));
                    dictionary.reset();
                    nextCode = RESTART + 1;
                    // Re-initialize the dictionary after reset
                    for (int i = 0; i < RESTART; i++) {
                        dictionary.put("" + (char) i, i);
                    }
                    dictionary.put( "",RESTART);
                }
                current = "" + c;
            }
        }
   
        // Update maxEvictions if necessary
        if (evictions > maxEvictions) {
            maxEvictions = evictions;
        }
   
        // Reset evictions for the next compression operation
        evictions = 0;
   
        if (dictionary.get(current) != null) {
            compressedList.add(dictionary.get(current));
        }
   
        // Update compression statistics
        numberOfCompressions++;
   
        byte[] compressed = new byte[compressedList.size() * 2]; // 2 bytes per code
        for (int i = 0; i < compressedList.size(); i++) {
            int code = compressedList.get(i);
            compressed[i * 2] = (byte) (code >> 8);
            compressed[i * 2 + 1] = (byte) code;
        }
   
        return compressed;
    }    

        //chatGpt code
        public String decompress(byte[] compressed) {
            MyCuckooTable<Integer, String> dictionary = new MyCuckooTable<>();
            int codeLength = INITIAL_CODE_LENGTH;
            int nextCode = RESTART + 1;
        
            // Initialize the dictionary with all 1-byte strings
            for (int i = 0; i < RESTART; i++) {
                dictionary.put(i, "" + (char) i);
            }
            dictionary.put(RESTART, "");
        
            StringBuilder decompressedText = new StringBuilder();
            String prevSequence = "";
            String sequence = "";
            boolean worked = true;
        
            System.out.println("Starting decompression");
        
            for (int i = 0; i < compressed.length; i += 2) {
                int code = ((compressed[i] & 0xFF) << 8) | (compressed[i + 1] & 0xFF);
        
                System.out.println("Processing code: " + code);
        
                if (code == RESTART) {
                    System.out.println("RESTART code encountered");
                    dictionary.reset();
                    codeLength = INITIAL_CODE_LENGTH;
                    nextCode = RESTART + 1;
                    for (int j = 0; j < RESTART; j++) {
                        dictionary.put(j, "" + (char) j);
                    }
                    prevSequence = "";
                    continue;
                }
        
                sequence = dictionary.get(code);
                if (sequence == null) {
                    if (code == nextCode) {
                        sequence = prevSequence + prevSequence.charAt(0);
                    } else {
                        throw new RuntimeException("Error: Unexpected code " + code);
                    }
                }
        
                System.out.println("Appending sequence: " + sequence);
                decompressedText.append(sequence);
        
                if (!prevSequence.isEmpty()) {
                    String newSequence = prevSequence + sequence.charAt(0);
                    System.out.println("Adding to dictionary: " + nextCode + " -> " + newSequence);
                    if (nextCode < (1 << codeLength)) {
                        worked = dictionary.put(nextCode++, newSequence);
                        if (!worked) {
                            System.out.println("didnt add to dictionary");
                            for (int j = 0; j < RESTART; j++) {
                                dictionary.put(j, "" + (char) j);
                            }
                            prevSequence = "";
                            codeLength = INITIAL_CODE_LENGTH;
                            nextCode = RESTART + 1;
                        }
                    } else if (codeLength < MAX_CODE_LENGTH) {
                        codeLength++;
                    }
                }
        
                prevSequence = sequence;
            }
        
            System.out.println("Decompression complete");
        
            return decompressedText.toString();
        }
    //         public String decompress(byte[] compressed) {
    //     MyCuckooTable<Integer, String> dictionary = new MyCuckooTable<>();
    //     int codeLength = INITIAL_CODE_LENGTH;
    //     int nextCode = RESTART + 1;
    //     int prevCode = -1;
    //     for (int i = 0; i < RESTART; i++) {
    //         dictionary.put(i, "" + (char) i);
    //     }
    //     StringBuilder output = new StringBuilder();
    //     for (int i = 0; i < compressed.length; i += 2) {
    //         if (i == 566) {
    //             int j = 90;
    //         }
    //         int code = ((compressed[i] & 0xFF) << 8) | (compressed[i + 1] & 0xFF);
    //         String sequence;
    //         if (code == RESTART) {
    //             dictionary.reset();
    //             codeLength = INITIAL_CODE_LENGTH;
    //             nextCode = RESTART + 1;
    //             // Rebuild the dictionary with the initial characters
    //             for (int j = 0; j < RESTART; j++) {
    //                 dictionary.put(j, "" + (char) j);
    //             }
    //             continue;
    //         } else if(dictionary.get(code) != null) {
    //             sequence = dictionary.get(code);
    //         } else if (code == nextCode) {
    //             sequence = dictionary.get(prevCode) + dictionary.get(prevCode).charAt(0);
    //         } else {
    //             throw new RuntimeException("Error: Unexpected code " + code);
    //         }
    //         output.append(sequence);
    //         if (prevCode != -1 && nextCode < (1 << codeLength)) {
    //             dictionary.put(nextCode++, dictionary.get(prevCode) + sequence.charAt(0));
    //         }
    //         if (nextCode >= (1 << codeLength) && codeLength < MAX_CODE_LENGTH) {
    //             codeLength++;
    //         }
    //         prevCode = code;

    //     }
    //     return output.toString();
    // }    

    public static int getInitialCodeLength() {
        return INITIAL_CODE_LENGTH;
    }

    public int getMaxEvictions() {
        return maxEvictions;
    }

    public double getAverageEvictions() {
        if (numberOfCompressions == 0) {
            return 0.0;
        }
        return (double) totalEvictions / numberOfCompressions;
    }

    public int getNumberOfCompressions() {
        return numberOfCompressions;
    }
}

// this version lets you run the testLZW method in main (the one above can't)
// this cant decode either but kinda correctly compresses
/*import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

// no full package imports allowed, just class imports

public class LZW {
    public static final int RESTART = 256;
    private static final int INITIAL_CODE_LENGTH = 9;
    private static final int MAX_CODE_LENGTH = 16;

    private int maxEvictions;
    private int totalEvictions;
    private int numberOfCompressions;
    private MyCuckooTable<String, Integer> dictionary;

    public LZW() {
        maxEvictions = 0;
        totalEvictions = 0;
        numberOfCompressions = 0;
        dictionary = new MyCuckooTable<>();
        // Initialize the dictionary with one-byte strings
        for (int i = 0; i < 256; i++) {
            dictionary.put("" + (char) i, i);
        }
        // Add the restart code to the dictionary
        dictionary.put("", RESTART);
    }

    public byte[] compress(String fullText) {
        List<Integer> compressedList = new ArrayList<>();
        StringBuilder currentSequence = new StringBuilder();
        for (char c : fullText.toCharArray()) {
            String currentSymbol = currentSequence.toString() + c; // Convert StringBuilder to String
            Integer code = dictionary.get(currentSymbol); // Use Integer type for code
            if (code != null) {
                currentSequence.append(c);
            } else {
                Integer currentCode = dictionary.get(currentSequence.toString());
                if (currentCode != null) {
                    compressedList.add(currentCode);
                }
                if (dictionary.size() < (1 << MAX_CODE_LENGTH)) {
                    dictionary.put(currentSymbol, dictionary.size());
                } else {
                    dictionary.reset();
                    numberOfCompressions++;
                    totalEvictions += dictionary.getEvictions();
                    maxEvictions = Math.max(maxEvictions, dictionary.getEvictions());
                    dictionary.put("", RESTART);
                }
                currentSequence = new StringBuilder("" + c);
            }
        }
        Integer currentCode = dictionary.get(currentSequence.toString());
        if (currentCode != null) {
            compressedList.add(currentCode);
        }
        byte[] compressedArray = new byte[compressedList.size() * 2];
        for (int i = 0; i < compressedList.size(); i++) {
            int code = compressedList.get(i);
            compressedArray[i * 2] = (byte) ((code >> 8) & 0xFF);
            compressedArray[i * 2 + 1] = (byte) (code & 0xFF);
        }
        return compressedArray;
    }    

    public String decompress(byte[] compressed) {
        StringBuilder decompressedText = new StringBuilder();
        dictionary.reset();
        int codeLength = INITIAL_CODE_LENGTH;
        int nextCode = RESTART + 1;
        String prevSequence = "";
        for (int i = 0; i < compressed.length; i += 2) {
            int code = ((compressed[i] & 0xFF) << 8) | (compressed[i + 1] & 0xFF);
            String sequence;
            if (code == RESTART) {
                dictionary.reset();
                codeLength = INITIAL_CODE_LENGTH;
                nextCode = RESTART + 1;
                for (int j = 0; j < RESTART; j++) {
                    dictionary.put("" + (char) j, j);
                }
                prevSequence = "";
                continue;
            }
            if (code < nextCode) {
                Integer sequenceCode = dictionary.get(String.valueOf(code));
                sequence = sequenceCode != null ? sequenceCode.toString() : null;
            } else if (code == nextCode && !prevSequence.isEmpty()) {
                sequence = prevSequence + prevSequence.charAt(0);
                dictionary.put(sequence, nextCode++);
            } else {
                throw new RuntimeException("Error: Unexpected code " + code);
            }
            if (sequence != null) {
                decompressedText.append(sequence);
                if (!prevSequence.isEmpty() && nextCode < (1 << codeLength)) {
                    String newSequence = prevSequence + sequence.charAt(0);
                    dictionary.put(newSequence, nextCode++);
                }
                prevSequence = sequence;
            }
        }
        return decompressedText.toString();
    }    

    public static int getInitialCodeLength() {
        return INITIAL_CODE_LENGTH;
    }

    public int getMaxEvictions() {
        return maxEvictions;
    }

    public double getAverageEvictions() {
        if (numberOfCompressions == 0) {
            return 0.0;
        }
        return (double) totalEvictions / numberOfCompressions;
    }

    public int getNumberOfCompressions() {
        return numberOfCompressions;
    }
} */
