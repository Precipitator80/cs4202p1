package com.rainlandsociety;

import java.io.IOException;
import java.util.List;

import com.alibaba.fastjson2.annotation.JSONField;

public class CacheSimulator {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println(
                    "Invalid arguments. Arguments must be in the form:\n ./CacheSimulator <path to json cache file> <path to trace file>");
            System.exit(1);
        }

        CacheSimulator simulator = new CacheSimulator();
        try {
            simulator.caches = JsonHandler.readConfiguration(args[0]);

            simulator.simulateProgram(JsonHandler.readProgramTrace(args[1]));

            JsonHandler.printResults(simulator);
        } catch (IOException e) {
            System.err.println("Could not read cache information from file:\n" + e.getMessage());
        }
    }

    void simulateProgram(List<MemoryOp> programTrace) {
        // for (MemoryOp memoryOp : programTrace) {
        //     // 1. Extract relevant bits from the memory address to determine cache set, cache line, and offset.
        //     // 2. Use the cache organization to check for cache hits or misses.
        //     CacheResult cacheResult = simulateCache(memoryOp.memoryAddress);

        //     // 3. Update performance metrics based on cache hit or miss.
        //     updateMetrics(cacheResult);

        //     // 4. If it's a cache miss, handle fetching or evicting cache lines accordingly.
        //     if (cacheResult.isMiss()) {
        //         handleCacheMiss(memoryOp.memoryAddress, memoryOp.size);
        //     }
        // }
    }

    @JSONField(name = "caches")
    public List<Cache> caches;

    @JSONField(name = "main_memory_accesses")
    public int main_memory_accesses;
}
