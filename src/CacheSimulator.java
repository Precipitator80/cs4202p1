import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.alibaba.fastjson2.annotation.JSONField;

public class CacheSimulator {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(
                    "Invalid arguments. Arguments must be in the form:\n ./CacheSimulator <path to json cache file> <path to trace file>");
            System.exit(1);
        }

        CacheSimulator simulator = new CacheSimulator();
        try {
            // Read the cache configuration.
            simulator.caches = Utility.readConfiguration(args[0]);

            try (BufferedReader reader = new BufferedReader(new FileReader(args[1]))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    BinaryAddress memoryAddress = new BinaryAddress(Utility.hexToBinary(line.substring(17, 33)));
                    int size = Integer.parseInt(line.substring(36, 39));
                    simulator.simulateMemoryOp(memoryAddress, size, 0);
                }
            }

            // Main memory accesses is equal to misses of the lowest cache level.
            simulator.main_memory_accesses = simulator.caches.get(simulator.caches.size() - 1).misses;

            // Print the results of the simulation to the console.
            Utility.printResults(simulator);
        } catch (IOException e) {
            System.err.println("Could not read cache information from file:\n" + e.getMessage());
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    // Read the program trace.
    // List<MemoryOp> programTrace = Utility.readProgramTrace(args[1]);

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
    //         new MemoryOp("00007f3ba6b40075", "00007f3ba6b59e0e", 'W', 1));

    // Simulate the program trace.
    //simulator.simulateProgram(programTrace);

    // void simulateProgram(List<MemoryOp> programTrace) {
    //     // Carry out all the operations.
    //     for (MemoryOp memoryOp : programTrace) {
    //         simulateMemoryOp(memoryOp.memoryAddress, memoryOp.size, 0);
    //     }

    //     // Main memory accesses is equal to misses of the lowest cache level.
    //     main_memory_accesses = caches.get(caches.size() - 1).misses;
    // }

    // Correct name?
    /**
     * 
     * Possible cases:
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
     * 
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
     * 
     * @param memoryAddress
     * @param size
     * @param cacheIndex
     */
    void simulateMemoryOp(BinaryAddress memoryAddress, int size, int cacheIndex) {
        Cache cache = caches.get(cacheIndex);
        boolean hit = cache.performOperation(memoryAddress);
        cache.accesses++;
        if (hit && !fitsInLine(memoryAddress, size, cache)) {
            cache.blockOverruns++;
            if (cacheIndex > 0) {
                cache.hits--;
            }
            BinaryAddress nextBlock = memoryAddress.nextBlock(cache);
            simulateMemoryOp(nextBlock, size - cache.lineSize, cacheIndex);
        } else if (!hit) {
            if (cacheIndex < caches.size() - 1) {
                if (fitsInLine(memoryAddress, size, cache)) {
                    simulateMemoryOp(memoryAddress, size, cacheIndex + 1);
                } else {
                    cache.blockOverruns++;
                    if (cacheIndex > 0) {
                        cache.hits--;
                    }
                    simulateMemoryOp(memoryAddress, cache.lineSize, cacheIndex + 1);
                    BinaryAddress nextBlock = memoryAddress.nextBlock(cache);
                    simulateMemoryOp(nextBlock, size - cache.lineSize, cacheIndex);
                }
            } else if (!fitsInLine(memoryAddress, size, cache)) {
                BinaryAddress nextBlock = memoryAddress.nextBlock(cache);
                simulateMemoryOp(nextBlock, size - cache.lineSize, cacheIndex);
            }
        }
    }

    boolean fitsInLine(BinaryAddress memoryAddress, int size, Cache cache) {
        long offset = memoryAddress.getOffset(cache);
        int lineSize = cache.lineSize;
        boolean fits = (offset + size) <= lineSize;
        return fits;
    }

    @JSONField(name = "caches")
    public List<Cache> caches;

    @JSONField(name = "main_memory_accesses")
    public int main_memory_accesses;
}
