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
            JsonHandler.printResults(simulator);
        } catch (IOException e) {
            System.err.println("Could not read cache information from file:\n" + e.getMessage());
        }
    }

    @JSONField(name = "caches")
    public List<Cache> caches;

    @JSONField(name = "main_memory_accesses")
    public int main_memory_accesses;
}
