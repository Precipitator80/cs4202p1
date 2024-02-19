import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;

/**
 * Class to simulate a cache running a specific program.
 */
public class CacheSimulator {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(
                    "Invalid arguments. Arguments must be in the form:\n ./CacheSimulator <path to json cache file> <path to trace file>");
            System.exit(1);
        }

        new CacheSimulator().simulate(args[0], args[1]);
    }

    @JSONField(name = "caches", deserialize = false)
    public List<Cache> caches; // The caches to simulate.

    @JSONField(name = "main_memory_accesses", deserialize = false)
    public int main_memory_accesses; // The number of main memory accesses performed. Equal to the misses of the lowest cache level.

    /**
     * Simulates a cache running a specific program.
     * @param cacheConfigFileName The file name of the cache config.
     * @param programTraceFileName The file name of the program trace.
     */
    void simulate(String cacheConfigFileName, String programTraceFileName) {
        try {
            // Read the cache configuration.
            readConfiguration(cacheConfigFileName);

            // Read in and simulate each memory operation.
            try (BufferedReader reader = new BufferedReader(new FileReader(programTraceFileName))) {
                reader.lines().forEach(line -> {
                    BinaryAddress memoryAddress = new BinaryAddress(line.substring(17, 33));
                    int size = Integer.parseInt(line.substring(36, 39));
                    simulateMemoryOp(memoryAddress, size, 0);
                });
            }

            // The number of main memory accesses is equal to the misses of the lowest cache level.
            main_memory_accesses = caches.get(caches.size() - 1).misses;

            // Print the simulation data to the console.
            System.out.println(JSON.toJSONString(this));
        } catch (IOException e) {
            System.err.println("Could not read cache information from file:\n" + e.getMessage());
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * Simulates a memory operation.
     * @param memoryAddress The memory address of the operation.
     * @param size The size of the operation.
     * @param cacheIndex The index of the cache to simulate the operation on.
     */
    void simulateMemoryOp(BinaryAddress memoryAddress, int size, int cacheIndex) {
        Cache cache = caches.get(cacheIndex);
        boolean hit = cache.performOperation(memoryAddress);
        // On a miss, run the same memory operation on the next level if available.
        if (!hit && cacheIndex < caches.size() - 1) {
            simulateMemoryOp(memoryAddress, size, cacheIndex + 1);
        }

        // If the operation did not fit on the line, run it again for the next block on the same level.
        if (!fitsInLine(memoryAddress.getOffset(cache), size, cache.lineSize)) {
            // Mark this as an overrun and subtract a hit to avoid counting the overrun as a hit on the same level.
            cache.blockOverruns++;
            if (cacheIndex > 0) {
                cache.hits--;
            }

            // Get the next block and run the operation with the size remaining of the overrun.
            BinaryAddress nextBlock = memoryAddress.nextBlock(cache);
            simulateMemoryOp(nextBlock, size - cache.lineSize, cacheIndex);
        }
    }

    /**
     * Calculates whether a memory operation fits in a specific cache line.
     * @param offset The offset within the cache line.
     * @param opSize The size of the operation.
     * @param lineSize The size of the cache line.
     * @return Whether the memory operation fits in the cache line.
     */
    boolean fitsInLine(long offset, int opSize, int lineSize) {
        return (offset + opSize) <= lineSize;
    }

    /**
     * Special class to support loading caches via JSON parsing.
     * Holds a field for a cache list in addition to getter and setter methods used automatically.
     */
    public class CacheConfiguration {
        @JSONField(name = "caches")
        List<Cache> caches;

        public List<Cache> getCaches() {
            return caches;
        }

        public void setCaches(List<Cache> caches) {
            this.caches = caches;
        }
    }

    /**
     * Reads a cache configuration and initialises each cache read.
     * @param cacheConfigFileName The file name of the cache config.
     * @throws Exception If there is an issue reading the config file or initialising each cache.
     */
    public void readConfiguration(String cacheConfigFileName) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(cacheConfigFileName))) {
            // Read the JSON string and parse it to get cache information.
            String jsonString = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            CacheConfiguration cacheConfiguration = JSON.parseObject(jsonString, CacheConfiguration.class);

            // Initialise each of the caches.
            caches = cacheConfiguration.getCaches();
            for (Cache cache : caches) {
                cache.initialise();
            }
        }
    }

    //// LEGACY CODE AND DISCUSSION
    /**
    * Four cases of additional actions required after updating the tag of a line:
    * 
    * [Oriented by hit / miss and then fits in line / does not fit in line.]
    * Hit:
    *      hit++
    *      Fits in line:
    *          return
    *      Does not fit in line:
    *          Run again for the next block with reduced size.
    * Miss:
    *      miss++
    *      Fits in line:
    *          Run again in the next level with the same size.
    *      Does not fit in line:
    *          Run again for the same block with reduced size in the next level.
    *          Run again for the next block with reduced size in the same level.
    * 
    * [Oriented by fits in line / does not fit in line and then hit / miss.]
    * Fits in line:
    *      Hit:
    *          hit++
    *          return
    *      Miss:
    *          miss++
    *          Run again in the next level with the same size.
    * Does not fit in line:
    *      Hit:
    *          hit++
    *      Miss:
    *          miss++
    *          Run again for the same block with reduced size in the next level.
    *      Run again for the next block with reduced size in the same level.
    */

    //// Attempt at using FileChannel to speed up reading of the program trace. Result was much slower than BufferedReader.
    // import java.io.RandomAccessFile;
    // import java.nio.ByteBuffer;
    // import java.nio.channels.FileChannel;
    // import java.nio.charset.StandardCharsets;
    // import java.nio.file.Paths;
    // import java.nio.file.StandardOpenOption;
    // try (FileChannel channel = FileChannel.open(Paths.get(args[1]), StandardOpenOption.READ)) {
    //     long position = 17; // Start at the first memory address.
    //     long fileSize = channel.size();
    //
    //     // Create buffers for the address and size.
    //     ByteBuffer addressBuffer = ByteBuffer.allocate(16);
    //     ByteBuffer sizeBuffer = ByteBuffer.allocate(3);
    //
    //     while (position < fileSize) {
    //         addressBuffer.clear();
    //         channel.read(addressBuffer, position);
    //         addressBuffer.flip();
    //         String addressString = new String(addressBuffer.array(), StandardCharsets.UTF_8);
    //         BinaryAddress memoryAddress = new BinaryAddress(addressString);
    //
    //         // Move to the size part.
    //         position += 19;
    //
    //         sizeBuffer.clear();
    //         channel.read(sizeBuffer, position);
    //         sizeBuffer.flip();
    //         int size = Integer.parseInt(new String(sizeBuffer.array(), StandardCharsets.UTF_8));
    //         simulator.simulateMemoryOp(memoryAddress, size, 0);
    //
    //         position += 21; // Move to the next line
    //     }
    // } catch (IOException e) {
    //     e.printStackTrace();
    // }

    //// Previous structure of simulator, which read all memory operations into a list before carrying them out.
    //// Performing each operation as it is read avoids overhead from list allocation.
    // Read the program trace.
    // List<MemoryOp> programTrace = Utility.readProgramTrace(args[1]);
    //
    // Use just a few lines instead to test more quickly.
    // First 10 lines of bwaves.
    // List<MemoryOp> programTrace = List.of(
    //         new MemoryOp("00007f3ba6b3f2b3", "00007ffc39282538", 'W', 8),
    //         new MemoryOp("00007f3ba6b40054", "00007ffc39282530", 'W', 8),
    //         new MemoryOp("00007f3ba6b40058", "00007ffc39282528", 'W', 8),
    //         new MemoryOp("00007f3ba6b4005a", "00007ffc39282520", 'W', 8),
    //         new MemoryOp("00007f3ba6b4005c", "00007ffc39282518", 'W', 8),
    //         new MemoryOp("00007f3ba6b4005e", "00007ffc39282510", 'W', 8),
    //         new MemoryOp("00007f3ba6b40060", "00007ffc39282508", 'W', 8),
    //         new MemoryOp("00007f3ba6b40068", "00007ffc392824b8", 'W', 8),
    //         new MemoryOp("00007f3ba6b40075", "00007f3ba6b59e0e", 'R', 1),
    //         new MemoryOp("00007f3ba6b40075", "00007f3ba6b59e0e", 'W', 1));// Simulate the program trace.
    // simulator.simulateProgram(programTrace);
    //
    // void simulateProgram(List<MemoryOp> programTrace) {
    //     // Carry out all the operations.
    //     for (MemoryOp memoryOp : programTrace) {
    //         simulateMemoryOp(memoryOp.memoryAddress, memoryOp.size, 0);
    //     }
    //
    //     // Main memory accesses is equal to misses of the lowest cache level.
    //     main_memory_accesses = caches.get(caches.size() - 1).misses;
    // }

    //// A class to represent memory operations.
    //// Was deemed redundant as the program counter and op kind are ignored.
    //// The rest of the values can be passed directly instead of allocating an object.
    // import java.math.BigInteger;
    // public class MemoryOp {
    //     public MemoryOp(String programHex, String memoryHex, char kind, int size) {
    //         this(memoryHex, size);
    //         programCounterAddress = new BinaryAddress(programHex);
    //         switch (kind) {
    //             case 'R':
    //                 this.kind = Kind.R;
    //                 break;
    //             case 'W':
    //                 this.kind = Kind.W;
    //                 break;
    //             default:
    //                 throw new IllegalArgumentException("Memory operation kind must be either R or W!");
    //         }
    //     }
    //
    //     public MemoryOp(String memoryHex, int size) {
    //         memoryAddress = new BinaryAddress(memoryHex);
    //         this.size = size;
    //     }
    //
    //     enum Kind {
    //         R,
    //         W
    //     }
    //
    //     BinaryAddress programCounterAddress;
    //     BinaryAddress memoryAddress;
    //     Kind kind;
    //     public int size;
    // }

    //// Various versions of a method to read the full program trace as a list.
    //// The top version did have efficiency gains over the others, but was still redundant when running operations directly after reads.
    // public static List<MemoryOp> readProgramTrace(String fileName) throws IOException {
    //     try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
    //         return reader.lines()
    //                 .map(line -> {
    //                     String[] tokens = line.split("\\s+");
    //                     //String programHex = tokens[0];
    //                     String memoryHex = tokens[1];
    //                     //char kind = tokens[2].charAt(0);
    //                     int size = Integer.parseInt(tokens[3]);
    //                     //return new MemoryOp(programHex, memoryHex, kind, size);
    //                     return new MemoryOp(memoryHex, size);
    //                 })
    //                 .collect(Collectors.toList());
    //     }
    // }

    // public static List<MemoryOp> readProgramTrace(String fileName) throws IOException {
    //     try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
    //         List<MemoryOp> programTrace = new LinkedList<>();
    //         List<String> lines = reader.lines().collect(Collectors.toList());
    //         for (String line : lines) {
    //
    //             String[] tokens = line.split("\\s+");
    //             String programHex = tokens[0];
    //             String memoryHex = tokens[1];
    //             char kind = tokens[2].toCharArray()[0];
    //             int size = Integer.parseInt(tokens[3]);
    //
    //             programTrace.add(new MemoryOp(programHex, memoryHex, kind, size));
    //         }
    //         return programTrace;
    //     }
    // }

    // public static List<MemoryOp> readProgramTrace(String fileName) throws IOException {
    //     List<MemoryOp> programTrace = new LinkedList<>();
    //     try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
    //         String line;
    //         while ((line = reader.readLine()) != null) {
    //             String[] tokens = line.split("\\s+");
    //             String programHex = tokens[0];
    //             String memoryHex = tokens[1];
    //             char kind = tokens[2].toCharArray()[0];
    //             int size = Integer.parseInt(tokens[3]);
    //
    //             programTrace.add(new MemoryOp(programHex, memoryHex, kind, size));
    //         }
    //     }
    //     return programTrace;
    // }

    //// Some utility methods for working with the binary string representation of binary addresses.
    // public static long parseBinaryString(String binaryString) {
    //     return Long.parseLong(binaryString, 2);
    // }

    // public static String addOneToBinary(String binaryString, int index) {
    //     // Convert binary string to char array for easy manipulation
    //     char[] binaryArray = binaryString.toCharArray();
    //
    //     // Start from the rightmost bit (least significant bit)
    //     for (int i = index; i >= 0; i--) {
    //         if (binaryArray[i] == '0') {
    //             // If the bit is '0', change it to '1' and exit the loop
    //             binaryArray[i] = '1';
    //             break;
    //         } else {
    //             // If the bit is '1', change it to '0' and continue carrying over
    //             binaryArray[i] = '0';
    //         }
    //     }
    //
    //     // Convert the modified char array back to a string
    //     return String.valueOf(binaryArray);
    // }

    /**
     * Convert hex string to binary string -
     * CapnChaos -
     * https://stackoverflow.com/questions/8640803/convert-hex-string-to-binary-string -
     * Accessed 05.02.2024
     * @param hex The string in hexadecimal digits.
     * @return The string in binary digits.
     */
    // public static String hexToBinary(String hex) {
    //     int len = hex.length() * 4;
    //     String bin = new BigInteger(hex, 16).toString(2);
    //
    //     //left pad the string result with 0s if converting to BigInteger removes them.
    //     bin = padBinaryString(bin, len);
    //
    //     return bin;
    // }

    // public static String padBinaryString(String bin, int len) {
    //     if (bin.length() < len) {
    //         int diff = len - bin.length();
    //         String pad = "";
    //         for (int i = 0; i < diff; ++i) {
    //             pad = pad.concat("0");
    //         }
    //         bin = pad.concat(bin);
    //     }
    //     return bin;
    // }
}
