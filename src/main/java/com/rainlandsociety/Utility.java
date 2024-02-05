package com.rainlandsociety;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;

public class Utility {
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

    public static List<Cache> readConfiguration(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String jsonString = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println(jsonString);
            CacheConfiguration cacheConfiguration = JSON.parseObject(jsonString, CacheConfiguration.class);
            List<Cache> caches = cacheConfiguration.getCaches();
            for (Cache cache : caches) {
                cache.initialise();
            }
            return caches;
        }
    }

    public static List<MemoryOp> readProgramTrace(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            return reader.lines()
                    .map(line -> {
                        String[] tokens = line.split("\\s+");
                        String programHex = tokens[0];
                        String memoryHex = tokens[1];
                        char kind = tokens[2].charAt(0);
                        int size = Integer.parseInt(tokens[3]);
                        return new MemoryOp(programHex, memoryHex, kind, size);
                    })
                    .collect(Collectors.toList());
        }
    }

    // public static List<MemoryOp> readProgramTrace(String fileName) throws IOException {
    //     try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
    //         List<MemoryOp> programTrace = new LinkedList<>();
    //         List<String> lines = reader.lines().collect(Collectors.toList());
    //         for (String line : lines) {

    //             String[] tokens = line.split("\\s+");
    //             String programHex = tokens[0];
    //             String memoryHex = tokens[1];
    //             char kind = tokens[2].toCharArray()[0];
    //             int size = Integer.parseInt(tokens[3]);

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

    //             programTrace.add(new MemoryOp(programHex, memoryHex, kind, size));
    //         }
    //     }
    //     return programTrace;
    // }

    public static void printResults(CacheSimulator simulator) {
        String jsonObject = JSON.toJSONString(simulator);
        System.out.println(jsonObject);
    }

    public static double log2(int input) {
        return Math.log(input) / Math.log(2);
    }

    public static int parseBinaryString(String binaryString) {
        return Integer.parseInt(binaryString, 2);
    }

    /**
     * Convert hex string to binary string -
     * CapnChaos -
     * https://stackoverflow.com/questions/8640803/convert-hex-string-to-binary-string -
     * Accessed 05.02.2024
     * @param hex The string in hexadecimal digits.
     * @return The string in binary digits.
     */
    public static String hexToBinary(String hex) {
        int len = hex.length() * 4;
        String bin = new BigInteger(hex, 16).toString(2);

        //left pad the string result with 0s if converting to BigInteger removes them.
        if (bin.length() < len) {
            int diff = len - bin.length();
            String pad = "";
            for (int i = 0; i < diff; ++i) {
                pad = pad.concat("0");
            }
            bin = pad.concat(bin);
        }
        return bin;
    }
}