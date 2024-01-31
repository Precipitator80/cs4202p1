package com.rainlandsociety;

import java.util.ArrayList;
import java.util.List;

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

    @JSONField(name = "hits")
    public int hits;

    @JSONField(name = "misses")
    public int misses;

    /*
    void setup() {
        int numberOfLines = size / lineSize;
    
        lines = new ArrayList<>();
        for (int i = 0; i < numberOfLines; i++) {
            //lines.add();
        }
    
        indexSize = (int) log2(numberOfLines);
        offsetSize = (int) log2(lineSize);
        tagSize = 64 - indexSize - offsetSize;
    }
    
    double log2(int input) {
        return Math.log(input) / Math.log(2);
    }
    
    int tag(String address) {
        return 0;
    }
    
    private List<Byte> lines;
    private int indexSize;
    private int offsetSize;
    private int tagSize;
    */

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