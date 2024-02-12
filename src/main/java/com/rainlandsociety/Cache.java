package com.rainlandsociety;

import com.alibaba.fastjson2.annotation.JSONField;

public class Cache {
    // enum Kind {
    //     DIRECT,
    //     FULL,
    //     TWO_WAY,
    //     FOUR_WAY,
    //     EIGHT_WAY
    // }

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

    /**
     * Other variables.
     */
    private final int ADDRESS_SPACE_SIZE = 64; // The size of the address space in bits.
    private CacheLine[] lines; // The cache lines holding data.

    @JSONField(serialize = false, deserialize = false)
    private int indexSize; // The index size of the cache.
    @JSONField(serialize = false, deserialize = false)
    private int offsetSize; // The offset size of the cache.
    @JSONField(serialize = false, deserialize = false)
    private int tagSize; // The tag size of the cache.

    void initialise() {
        int numberOfLines = size / lineSize; // DIRECT CACHE

        lines = new CacheLine[numberOfLines];
        for (int i = 0; i < numberOfLines; i++) {
            lines[i] = new CacheLine();
        }

        indexSize = (int) Utility.log2(numberOfLines);
        offsetSize = (int) Utility.log2(lineSize);
        tagSize = ADDRESS_SPACE_SIZE - indexSize - offsetSize;
    }

    /**
     * Getters
     */
    public CacheLine getLine(int index) {
        return lines[index];
    }

    public int getIndexSize() {
        return indexSize;
    }

    public int getOffsetSize() {
        return offsetSize;
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