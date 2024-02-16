package com.rainlandsociety;

import java.util.Iterator;
import java.util.LinkedList;

import com.alibaba.fastjson2.annotation.JSONField;

public class Cache {
    // enum ReplacementPolicy {
    //     RR,
    //     LRU,
    //     LFU
    // }

    /**
     * JSON Fields
     */
    @JSONField(name = "name")
    public String name;

    @JSONField(name = "size", serialize = false)
    public int size;

    @JSONField(name = "line_size", serialize = false)
    public int lineSize;

    @JSONField(name = "kind", serialize = false)
    public String kind;

    @JSONField(name = "replacement_policy", serialize = false)
    public String replacementPolicy;

    @JSONField(name = "hits", deserialize = false)
    public int hits;

    @JSONField(name = "misses", deserialize = false)
    public int misses;

    @JSONField(name = "accesses", serialize = false, deserialize = false)
    public int accesses;

    @JSONField(name = "block_overruns", serialize = false, deserialize = false)
    public int blockOverruns;

    @JSONField(name = "block_overruns", serialize = false, deserialize = false)
    public int setSize;

    /**
     * Other variables.
     */
    private final int ADDRESS_SPACE_SIZE = 64; // The size of the address space in bits.
    private LinkedList<CacheLine>[] sets; // Sets of cache lines holding data.

    @JSONField(serialize = false, deserialize = false)
    private int setIdentSize; // The index size of the cache.
    @JSONField(serialize = false, deserialize = false)
    private int offsetIdentSize; // The offset size of the cache.
    @JSONField(serialize = false, deserialize = false)
    private int tagSize; // The tag size of the cache.

    void initialise() throws Exception {
        // Define the sets.
        int numberOfLines = size / lineSize;
        int numberOfSets;
        switch (kind) {
            case ("direct"):
                numberOfSets = numberOfLines;
                break;
            case ("full"):
                numberOfSets = 1;
                break;
            case ("2way"):
                numberOfSets = numberOfLines / 2;
                break;
            case ("4way"):
                numberOfSets = numberOfLines / 4;
                break;
            case ("8way"):
                numberOfSets = numberOfLines / 8;
                break;
            default:
                throw new Exception("The cache type '" + kind + "' is not supported by the simulator!");
        }
        setSize = numberOfLines / numberOfSets;

        // Initialise the cache lines.
        sets = new LinkedList[numberOfSets];
        for (int i = 0; i < numberOfSets; i++) {
            sets[i] = new LinkedList<>();
            for (int j = 0; j < setSize; j++) {
                sets[i].add(new CacheLine());
            }
        }

        setIdentSize = (int) Utility.log2(numberOfSets);
        offsetIdentSize = (int) Utility.log2(lineSize);
        tagSize = ADDRESS_SPACE_SIZE - setIdentSize - offsetIdentSize;
    }

    public boolean performOperation(BinaryAddress memoryAddress) {
        int setNumber = memoryAddress.getSet(this);
        long tag = memoryAddress.getTag(this);
        LinkedList<CacheLine> set = sets[setNumber];
        Iterator<CacheLine> setIterator = set.iterator();
        while (setIterator.hasNext()) {
            CacheLine line = setIterator.next();
            if (!line.getValid()) {
                // Compulsory miss!
                misses++;
                line.updateTag(tag);
                return false;
            } else if (line.getTag() == tag) {
                // Hit!
                hits++;
                return true;
            }
        }
        // Capacity / conflict miss! Evict and replace.
        // TODO
        // For now just choose first line (direct)
        misses++;
        set.peek().updateTag(tag);
        return false;
    }

    /**
     * Getters
     */
    public int getSetIdentSize() {
        return setIdentSize;
    }

    public int getOffsetIdentSize() {
        return offsetIdentSize;
    }

    public int getTagSize() {
        return tagSize;
    }

    /*
    First 10 lines of bwaves.
    16-digit 64-bit hexadecimal values, corresponding to a 64-bit
    simulated address space. Therefore, pointers are 64-bits in size.
    number_of_lines = cache_size / line_size
    index = log_2(number_of_lines)
    offset = log_2(line_size)
    tag = address_size - offset - index.
    00007f3ba6b3f2b3 00007ffc39282538 W 008
    00007f3ba6b40054 00007ffc39282530 W 008
    00007f3ba6b40058 00007ffc39282528 W 008
    00007f3ba6b4005a 00007ffc39282520 W 008
    00007f3ba6b4005c 00007ffc39282518 W 008
    00007f3ba6b4005e 00007ffc39282510 W 008
    00007f3ba6b40060 00007ffc39282508 W 008
    00007f3ba6b40068 00007ffc392824b8 W 008
    00007f3ba6b40075 00007f3ba6b59e0e R 001
    00007f3ba6b40075 00007f3ba6b59e0e W 001
     */
}