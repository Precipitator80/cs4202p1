import java.io.BufferedReader;
import java.io.FileReader;
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

    public static List<Cache> readConfiguration(String fileName) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String jsonString = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            CacheConfiguration cacheConfiguration = JSON.parseObject(jsonString, CacheConfiguration.class);
            List<Cache> caches = cacheConfiguration.getCaches();
            for (Cache cache : caches) {
                cache.initialise();
            }
            return caches;
        }
    }

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

    public static long hexToBinary(String hexString) {
        return Long.parseLong(hexString, 16);
    }

    public static long incrementAddress(long address, int index) {
        // Fix later...

        long number = 10; // Example long number
        int bitPosition = 60; // Bit position to increment (0-indexed)
        // Check if the bit at the specified position is already 1
        if ((number & (1L << (63 - bitPosition))) != 0) {
            // Carry
            int carryPosition = 63 - bitPosition;
            while ((number & (1L << carryPosition)) != 0) {
                number &= ~(1L << carryPosition); // Clear the bit
                carryPosition--; // Move to the previous bit position
            }
            number |= (1L << carryPosition); // Set the previous bit
        } else {
            // Bit at the specified position is 0, just set it to 1
            number |= (1L << (63 - bitPosition));
        }

        System.out.println("Original number: " + Long.toBinaryString(10));
        System.out.println(
                "Number after incrementing bit at position " + bitPosition + ": " + Long.toBinaryString(number));

        address = 80;
        index = 60;
        String binaryBefore = Long.toBinaryString(address);

        // Create a mask to set the bit at the specified index
        long mask = 1L << (Cache.ADDRESS_SPACE_SIZE - index);

        // Set the bit at the specified index
        address = address |= mask;

        // Propagate carry
        for (int i = index; i >= 0; i--) {
            if ((address & (1L << i)) == 0) {
                // If the bit at index i is 0, carry propagation stops
                break;
            } else {
                // If the bit at index i is 1, clear it and continue carry propagation
                address = address &= ~(1L << i);
            }
        }

        String binaryAfter = Long.toBinaryString(address);

        return address;
    }

    // public static long parseBinaryString(String binaryString) {
    //     return Long.parseLong(binaryString, 2);
    // }

    // public static String addOneToBinary(String binaryString, int index) {
    //     // Convert binary string to char array for easy manipulation
    //     char[] binaryArray = binaryString.toCharArray();

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

    //     //left pad the string result with 0s if converting to BigInteger removes them.
    //     bin = padBinaryString(bin, len);

    //     return bin;
    // }

    public static String padBinaryString(String bin, int len) {
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