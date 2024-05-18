/* Authors: Qaturi Vaughn & Moses Tennent
 * Assignment 4, TCSS 342 Spring 2024
 * This is the Main class for the compressed literature 2 assignment
 * 
 * The Main class controls the running and testing of the compression 
 * algorithm. It reads in input from a text file specified as a command line argument
 * and compresses the content. It outputs the compressed data to compressed.bin,
 * decompresses data to decompressed.txt, and displays statistics
 * 
 * 
 * It was taking 10 minutes for the decompressed to work and outputed the right kibibyytes
 * and actually matched the original file, but after running it for awhile it now takes less 
 * than a second but under 500 kibibytes / doesnt actually match the orginal but claims to
 * which we cant figure out yet. We will be working on it after the submission deadline.
 * So we will submit another version if we figure out how to correctly do it
 */
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException; 

// imports allowed java.io.*, java.nio.*, ArrayList, Byte, and List<Byte>

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Main <c|d> <inputfile> <outputfile>");
            return;
        }
        testLZW(); 
        testMyCuckooTable();

        String command = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

        LZW lzw = new LZW();

        if (command.equals("c")) {
            // Compress
            System.out.println("Input file: " + inputFile);
            long startRead = System.currentTimeMillis();
            String fullText = readFile(inputFile);
            long endRead = System.currentTimeMillis();
            int originalSize = fullText.getBytes().length * 8; // in bits
            System.out.println("original length: " + originalSize / 8 + " bytes = " + originalSize / 8192 + " kibibytes.");
            System.out.println("Reading time: " + (endRead - startRead) + ".0 milliseconds");

            long startEncode = System.currentTimeMillis();
            byte[] compressed = lzw.compress(fullText);
            long endEncode = System.currentTimeMillis();
            writeBinaryFile(outputFile, compressed);

            // Measure compression ratio
            int compressedSize = compressed.length * 8; // in bits
            double compressionRatio = (1 - (double) compressedSize / originalSize) * 100;

            // Calculate total number of LZW codes
            int codeLength = LZW.getInitialCodeLength();
            int totalCodes = compressedSize / codeLength;

            // Output encoding information
            System.out.println("\n**** ENCODING ****");
            System.out.println("total number of LZW codes: " + totalCodes);
            System.out.println("compressed size: " + compressedSize + " bits = " + (compressedSize / 8) + " bytes = " + (compressedSize / 8192) + " kibibytes.");
            System.out.println("compression ratio: " + String.format("%.2f", compressionRatio) + "%");
            System.out.println("maximum number of evictions: " + lzw.getMaxEvictions());
            System.out.println("average number of evictions: " + String.format("%.7f", lzw.getAverageEvictions()));
            System.out.println("encoding time: " + (endEncode - startEncode) + ".0 milliseconds");
        } else if (command.equals("d")) {
            // Decompress
            System.out.println("Input file: " + inputFile);
            long startRead = System.currentTimeMillis();
            String allText = readFile(inputFile);
            long endRead = System.currentTimeMillis();
            int originalSize = allText.getBytes().length * 8; // in bits
            System.out.println("original length: " + originalSize / 8 + " bytes = " + originalSize / 8192 + " kibibytes.");
            System.out.println("Reading time: " + (endRead - startRead) + ".0 milliseconds");

            byte[] compressed = readBinaryFile(inputFile);
            long startDecode = System.currentTimeMillis();
            String fullText = lzw.decompress(compressed);
            long endDecode = System.currentTimeMillis();
            writeFile(outputFile, fullText);

            // Output decoding information
            System.out.println("\n**** DECODING ****");
            System.out.println("decoded length: " + fullText.getBytes().length + " bytes = " + (fullText.getBytes().length / 1024) + " kibibytes.");
            System.out.println("decoding time: " + (endDecode - startDecode) + ".0 milliseconds");

            // Compare the decompressed file with the original
            String original = readFile(outputFile);
            /*String original;
            if (command.equals("c")) {
                original = readFile(inputFile);
            } else {
                original = readFile(outputFile);
            }*/
            if (original.equals(fullText)) {
                System.out.println("\n**** COMPARING ****");
                System.out.println("Decompressed file MATCHES the original!");
            } else {
                System.out.println("\n**** COMPARING ****");
                System.out.println("Decompressed file does NOT match the original!");
            }
        } else {
            System.out.println("Invalid command. Use 'c' for compress and 'd' for decompress.");
        }
    }

    private static String readFile(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return null;
        }
    }

    private static byte[] readBinaryFile(String filename) {
        try {
            return Files.readAllBytes(Paths.get(filename));
        } catch (IOException e) {
            System.out.println("Error reading binary file: " + e.getMessage());
            return null;
        }
    }

    private static void writeFile(String filename, String contents) {
        try {
            Files.write(Paths.get(filename), contents.getBytes());
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }

    private static void writeBinaryFile(String filename, byte[] contents) {
        try {
            Files.write(Paths.get(filename), contents);
        } catch (IOException e) {
            System.out.println("Error writing binary file: " + e.getMessage());
        }
    }
    public static void testLZW() {
        System.out.println("Testing LZW.java\n");
        LZW lzw = new LZW();

        // Test strings for compression and decompression
        String[] testStrings = {
                "Hello, world!",
                "The quick brown fox jumps over the lazy dog",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                "" // Testing with an empty string
        };

        for (String original : testStrings) {
            // Compress the original string
            byte[] compressed = lzw.compress(original);

            // Decompress the compressed data
            String decompressed = lzw.decompress(compressed);

            // Check if the original string and the decompressed string are equal
            boolean testPassed = original.equals(decompressed);

            System.out.println("Original: " + original);
            System.out.println("Compressed: " + byteArrayToString(compressed));
            System.out.println("Decompressed: " + decompressed);
            System.out.println("Test passed: " + testPassed);
            System.out.println();
        }
    }

    public static void testMyCuckooTable() {
        System.out.println("Testing MyCuckooTable.java\n");
        MyCuckooTable<String, String> table = new MyCuckooTable<>();

        // Test the put method
        System.out.println("Adding key-value pairs to the table...");
        table.put("key1", "value1");
        table.put("key2", "value2");
        table.put("key3", "value3");

        // Test the get method
        System.out.println("Retrieving values from the table...");
        System.out.println("key1: " + table.get("key1")); // Should print "value1"
        System.out.println("key2: " + table.get("key2")); // Should print "value2"
        System.out.println("key3: " + table.get("key3")); // Should print "value3"

        // Test the reset method
        System.out.println("Resetting the table...");
        table.reset();
        System.out.println("key1: " + table.get("key1")); // Should print "null"
        System.out.println("key2: " + table.get("key2")); // Should print "null"
        System.out.println("key3: " + table.get("key3") + "\n"); // Should print "null"
    }

    private static String byteArrayToString(byte[] array) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(array[i]);
        }
        builder.append("]");
        return builder.toString();
    }
}
