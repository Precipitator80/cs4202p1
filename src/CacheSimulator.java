import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

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
            caches = Utility.readConfiguration(cacheConfigFileName);

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
}
